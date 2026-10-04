package com.hotel.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hotel.model.ComplaintClassificationResult;

import java.util.Arrays;
import java.util.List;

/**
 * ComplaintClassificationService - Stage 14 AI Complaint Classifier.
 * Analyzes natural language guest complaints using Google Gemini to extract:
 * 1. Target Department Category (Maintenance, Housekeeping, Food Service, Security, Reception)
 * 2. Urgency Priority Level (LOW, NORMAL, HIGH, URGENT)
 * 3. Concise Issue Ticket Summary (4-8 words)
 * 4. Empathetic Guest Reassurance Message
 * 
 * Provides robust deterministic keyword fallback if Gemini is offline or unconfigured.
 */
public class ComplaintClassificationService {

    private static final List<String> VALID_CATEGORIES = Arrays.asList(
            "Maintenance", "Housekeeping", "Food Service", "Security", "Reception"
    );

    private static final List<String> VALID_PRIORITIES = Arrays.asList(
            "LOW", "NORMAL", "HIGH", "URGENT"
    );

    private final GeminiService geminiService;

    public ComplaintClassificationService() {
        this.geminiService = new GeminiService();
    }

    public ComplaintClassificationService(GeminiService geminiService) {
        this.geminiService = (geminiService != null) ? geminiService : new GeminiService();
    }

    /**
     * Classifies a guest's issue description into structured operational ticket data.
     * 
     * @param rawText Natural language complaint text from guest
     * @param manualCategory Optional manual category chosen by guest ("AUTO" or specific)
     * @param manualPriority Optional manual priority chosen by guest ("AUTO" or specific)
     * @return ComplaintClassificationResult containing classified metadata
     */
    public ComplaintClassificationResult classifyComplaint(String rawText, String manualCategory, String manualPriority) {
        String cleanText = (rawText != null) ? rawText.trim() : "";

        // Attempt AI classification via Google Gemini
        if (geminiService.isApiKeyConfigured() && !cleanText.isEmpty()) {
            try {
                String systemInstruction = "You are an expert AI Hotel Operations Dispatcher for an AI Smart Hotel Management System.\n"
                        + "Analyze the guest's complaint text and classify it into structured operational parameters.\n\n"
                        + "Strict Requirements:\n"
                        + "1. \"category\": Must be strictly one of [\"Maintenance\", \"Housekeeping\", \"Food Service\", \"Security\", \"Reception\"].\n"
                        + "   - \"Maintenance\": AC, plumbing, water leak, heating, electrical, lights, appliances, door locks.\n"
                        + "   - \"Housekeeping\": Dirty room, linen, towels, hygiene, trash, cleaning request.\n"
                        + "   - \"Food Service\": Room service orders, dining quality, minibar, cold food, breakfast delivery.\n"
                        + "   - \"Security\": Loud noise, disturbance, unauthorized persons, safety, safe locks.\n"
                        + "   - \"Reception\": Keycard failure, billing, check-in questions, luggage.\n"
                        + "2. \"priority\": Must be strictly one of [\"LOW\", \"NORMAL\", \"HIGH\", \"URGENT\"].\n"
                        + "   - \"URGENT\": Active flooding, electrical sparks/smoke, door locked from outside at night, severe safety.\n"
                        + "   - \"HIGH\": AC failure in extreme weather, no water/hot water, broken toilet, major disruption.\n"
                        + "   - \"NORMAL\": Missing towels, sluggish Wi-Fi, minor appliance glitch, noisy neighbors.\n"
                        + "   - \"LOW\": General cosmetic feedback, minor request.\n"
                        + "3. \"shortDescription\": A concise 4 to 8 word issue summary suitable for staff ticket title.\n"
                        + "4. \"reassuranceMessage\": A warm, professional 1-2 sentence response apologizing for the trouble and reassuring the guest that our team is taking immediate action.\n\n"
                        + "Output strictly a single JSON object with keys: category, priority, shortDescription, reassuranceMessage. Do not include markdown codeblocks or extra text.";

                String userPrompt = "Guest Complaint Description:\n\"" + cleanText + "\"";

                String aiResponse = geminiService.callGenerateContent(systemInstruction, userPrompt);

                ComplaintClassificationResult result = parseGeminiResponse(aiResponse);
                if (result != null) {
                    // Apply manual overrides if explicitly requested by user
                    if (manualCategory != null && !manualCategory.equalsIgnoreCase("AUTO") && !manualCategory.trim().isEmpty()) {
                        result.setCategory(manualCategory.trim());
                    }
                    if (manualPriority != null && !manualPriority.equalsIgnoreCase("AUTO") && !manualPriority.trim().isEmpty()) {
                        result.setPriority(manualPriority.trim());
                    }
                    return result;
                }
            } catch (Exception e) {
                System.err.println("Warning: Gemini AI classification failed, using intelligent heuristic fallback: " + e.getMessage());
            }
        }

        // Resilient Heuristic Fallback
        return fallbackClassification(cleanText, manualCategory, manualPriority);
    }

    /**
     * Parses the JSON payload returned by Google Gemini.
     */
    private ComplaintClassificationResult parseGeminiResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return null;
        }

        String jsonText = rawResponse.trim();
        // Strip markdown code fences if Gemini added them (e.g. ```json ... ```)
        if (jsonText.startsWith("```")) {
            int firstNewline = jsonText.indexOf('\n');
            int lastFence = jsonText.lastIndexOf("```");
            if (firstNewline != -1 && lastFence > firstNewline) {
                jsonText = jsonText.substring(firstNewline + 1, lastFence).trim();
            }
        }

        try {
            JsonObject obj = JsonParser.parseString(jsonText).getAsJsonObject();

            String category = obj.has("category") ? obj.get("category").getAsString().trim() : "Maintenance";
            String priority = obj.has("priority") ? obj.get("priority").getAsString().trim().toUpperCase() : "NORMAL";
            String shortDesc = obj.has("shortDescription") ? obj.get("shortDescription").getAsString().trim() : "";
            String reassurance = obj.has("reassuranceMessage") ? obj.get("reassuranceMessage").getAsString().trim() : "";

            // Validate against allowed categories
            if (!VALID_CATEGORIES.contains(category)) {
                category = matchClosestCategory(category);
            }

            // Validate against allowed priorities
            if (!VALID_PRIORITIES.contains(priority)) {
                priority = "NORMAL";
            }

            if (shortDesc.isEmpty()) {
                shortDesc = "Guest reported " + category.toLowerCase() + " issue";
            }

            if (reassurance.isEmpty()) {
                reassurance = "Thank you for bringing this to our attention. Our " + category + " team has been notified and is addressing your request.";
            }

            return new ComplaintClassificationResult(category, priority, shortDesc, reassurance, true);
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deterministic keyword classification fallback when AI is unavailable or offline.
     */
    public ComplaintClassificationResult fallbackClassification(String cleanText, String manualCategory, String manualPriority) {
        String lower = cleanText.toLowerCase();

        String category;
        String priority;

        // 1. Determine Category
        if (manualCategory != null && !manualCategory.equalsIgnoreCase("AUTO") && !manualCategory.trim().isEmpty()) {
            category = manualCategory.trim();
        } else if (lower.contains("ac") || lower.contains("air condition") || lower.contains("leak") || lower.contains("pipe") || lower.contains("plumb") || lower.contains("drain") || lower.contains("heater") || lower.contains("electric") || lower.contains("light") || lower.contains("tv") || lower.contains("toilet") || lower.contains("broken")) {
            category = "Maintenance";
        } else if (lower.contains("clean") || lower.contains("towel") || lower.contains("linen") || lower.contains("sheet") || lower.contains("trash") || lower.contains("dust") || lower.contains("stain")) {
            category = "Housekeeping";
        } else if (lower.contains("food") || lower.contains("drink") || lower.contains("dining") || lower.contains("breakfast") || lower.contains("dinner") || lower.contains("water bottle") || lower.contains("cold food")) {
            category = "Food Service";
        } else if (lower.contains("noise") || lower.contains("loud") || lower.contains("security") || lower.contains("shouting") || lower.contains("stranger") || lower.contains("safe key")) {
            category = "Security";
        } else if (lower.contains("keycard") || lower.contains("bill") || lower.contains("charge") || lower.contains("reception") || lower.contains("front desk") || lower.contains("check-in") || lower.contains("checkout")) {
            category = "Reception";
        } else {
            category = "Maintenance";
        }

        // 2. Determine Priority
        if (manualPriority != null && !manualPriority.equalsIgnoreCase("AUTO") && !manualPriority.trim().isEmpty()) {
            priority = manualPriority.trim();
        } else if (lower.contains("flood") || lower.contains("spark") || lower.contains("smoke") || lower.contains("fire") || lower.contains("emergency") || lower.contains("danger") || lower.contains("locked out")) {
            priority = "URGENT";
        } else if (lower.contains("leak") || lower.contains("ac") || lower.contains("air conditioning") || lower.contains("no water") || lower.contains("hot water") || lower.contains("broken") || lower.contains("toilet")) {
            priority = "HIGH";
        } else if (lower.contains("slow") || lower.contains("wifi") || lower.contains("dirty") || lower.contains("smell")) {
            priority = "NORMAL";
        } else {
            priority = "LOW";
        }

        String shortDesc = cleanText.length() > 50 ? cleanText.substring(0, 47) + "..." : cleanText;
        if (shortDesc.isEmpty()) {
            shortDesc = category + " Issue Reported";
        }

        String reassurance = "Your " + category.toLowerCase() + " request has been registered with " + priority.toLowerCase() + " priority. Our hotel staff is attending to it shortly.";

        return new ComplaintClassificationResult(category, priority, shortDesc, reassurance, false);
    }

    private String matchClosestCategory(String input) {
        String lower = (input != null) ? input.toLowerCase() : "";
        if (lower.contains("clean") || lower.contains("housekeep")) return "Housekeeping";
        if (lower.contains("food") || lower.contains("dine") || lower.contains("kitchen")) return "Food Service";
        if (lower.contains("security") || lower.contains("noise")) return "Security";
        if (lower.contains("reception") || lower.contains("front") || lower.contains("desk")) return "Reception";
        return "Maintenance";
    }
}
