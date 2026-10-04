package com.hotel.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hotel.model.ServiceRequestAIResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ServiceRequestUnderstandingService - Stage 15 AI Service Request Understanding & Smart Item Extraction.
 * Analyzes natural language guest requests using Google Gemini to extract:
 * 1. Target Department (Housekeeping, Food Service, Maintenance, Reception)
 * 2. Quantified Item Extraction (e.g. "2x Bath Towels, 1x Sparkling Water")
 * 3. Priority Assessment (LOW, NORMAL, HIGH, URGENT)
 * 4. Realistic Estimated Fulfillment Time (ETA)
 * 5. Warm Guest Confirmation & Reassurance Message
 *
 * Includes a robust deterministic heuristic fallback if Gemini is offline or rate-limited.
 */
public class ServiceRequestUnderstandingService {

    private static final List<String> VALID_REQUEST_TYPES = Arrays.asList(
            "Housekeeping", "Food Service", "Maintenance", "Reception"
    );

    private static final List<String> VALID_PRIORITIES = Arrays.asList(
            "LOW", "NORMAL", "HIGH", "URGENT"
    );

    private final GeminiService geminiService;

    public ServiceRequestUnderstandingService() {
        this.geminiService = new GeminiService();
    }

    public ServiceRequestUnderstandingService(GeminiService geminiService) {
        this.geminiService = (geminiService != null) ? geminiService : new GeminiService();
    }

    /**
     * Parses and understands a guest's natural language room service or housekeeping request.
     *
     * @param rawText Natural language request entered by guest
     * @param manualType Optional manual department selection ("AUTO" or specific)
     * @param manualPriority Optional manual priority selection ("AUTO" or specific)
     * @return ServiceRequestAIResult with structured items, category, priority, ETA, and reassurance
     */
    public ServiceRequestAIResult analyzeRequest(String rawText, String manualType, String manualPriority) {
        String cleanText = (rawText != null) ? rawText.trim() : "";

        // 1. Attempt AI analysis via Google Gemini API
        if (geminiService.isApiKeyConfigured() && !cleanText.isEmpty()) {
            try {
                String systemInstruction = "You are an intelligent Room Service & Housekeeping Assistant for a 5-star AI Smart Hotel Management System.\n"
                        + "Analyze the guest's natural language room service request and extract structured items, operational department, priority, estimated completion time, and a warm reassurance message.\n\n"
                        + "Strict Requirements:\n"
                        + "1. \"requestType\": Strictly one of [\"Housekeeping\", \"Food Service\", \"Maintenance\", \"Reception\"].\n"
                        + "   - \"Housekeeping\": Towels, linens, bathrobes, pillows, blankets, toiletries, shampoo, dental kits, room cleaning, trash pickup.\n"
                        + "   - \"Food Service\": Food, meals, beverages, coffee, tea, water bottles, sandwiches, room dining orders, snacks.\n"
                        + "   - \"Maintenance\": Iron & ironing board, hairdryer, adapter, electrical plug, remote battery, safe box.\n"
                        + "   - \"Reception\": Keycard reissue, luggage pickup, wake-up call, concierge inquiries.\n"
                        + "2. \"extractedItems\": A clean, quantified comma-separated summary of items (e.g. \"2x Extra Bath Towels, 1x Mineral Water, 2x Feather Pillows\"). If no specific countable items are mentioned, provide a clear task summary (e.g. \"Room Cleaning & Bed Linen Change\").\n"
                        + "3. \"priority\": Strictly one of [\"LOW\", \"NORMAL\", \"HIGH\", \"URGENT\"].\n"
                        + "   - \"URGENT\": Emergency needs (baby food/milk, medical ice pack, hazardous glass clean-up).\n"
                        + "   - \"HIGH\": Time-sensitive dining before checkout, infant crib, critical request.\n"
                        + "   - \"NORMAL\": Standard room service, extra towels, beverages, extra pillows, toiletries.\n"
                        + "   - \"LOW\": Non-urgent routine amenities.\n"
                        + "4. \"estimatedMinutes\": A realistic estimated fulfillment time string (e.g. \"10-15 mins\", \"15-20 mins\", \"20-30 mins\").\n"
                        + "5. \"confirmationMessage\": A warm, polite 1-2 sentence reassurance acknowledging the exact items requested and confirming our staff is preparing and delivering them to their room shortly.\n\n"
                        + "Output strictly a single JSON object with keys: requestType, extractedItems, priority, estimatedMinutes, confirmationMessage. Do not include markdown codeblocks or extra text.";

                String userPrompt = "Guest Service Request:\n\"" + cleanText + "\"";

                String aiResponse = geminiService.callGenerateContent(systemInstruction, userPrompt);

                ServiceRequestAIResult result = parseGeminiResponse(aiResponse);
                if (result != null) {
                    // Apply manual overrides if explicitly requested by guest
                    if (manualType != null && !manualType.equalsIgnoreCase("AUTO") && !manualType.trim().isEmpty()) {
                        result.setRequestType(manualType.trim());
                    }
                    if (manualPriority != null && !manualPriority.equalsIgnoreCase("AUTO") && !manualPriority.trim().isEmpty()) {
                        result.setPriority(manualPriority.trim());
                    }
                    return result;
                }
            } catch (Exception e) {
                System.err.println("[ServiceRequestUnderstandingService] Gemini AI parsing error: " + e.getMessage() + ". Falling back to heuristic rules.");
            }
        }

        // 2. Deterministic Rule-Based Fallback
        return fallbackExtraction(cleanText, manualType, manualPriority);
    }

    /**
     * Parses raw JSON string returned by Google Gemini.
     */
    private ServiceRequestAIResult parseGeminiResponse(String rawJson) {
        if (rawJson == null || rawJson.trim().isEmpty()) {
            return null;
        }

        try {
            String sanitized = rawJson.trim();
            int startIdx = sanitized.indexOf('{');
            int endIdx = sanitized.lastIndexOf('}');
            if (startIdx != -1 && endIdx > startIdx) {
                sanitized = sanitized.substring(startIdx, endIdx + 1).trim();
            }

            JsonObject obj = JsonParser.parseString(sanitized).getAsJsonObject();

            String requestType = obj.has("requestType") ? obj.get("requestType").getAsString().trim() : "Housekeeping";
            String extractedItems = obj.has("extractedItems") ? obj.get("extractedItems").getAsString().trim() : "";
            String priority = obj.has("priority") ? obj.get("priority").getAsString().trim().toUpperCase() : "NORMAL";
            String estimatedMinutes = obj.has("estimatedMinutes") ? obj.get("estimatedMinutes").getAsString().trim() : "15-20 mins";
            String confirmationMessage = obj.has("confirmationMessage") ? obj.get("confirmationMessage").getAsString().trim() : "";

            if (!VALID_REQUEST_TYPES.contains(requestType)) {
                requestType = "Housekeeping";
            }
            if (!VALID_PRIORITIES.contains(priority)) {
                priority = "NORMAL";
            }

            if (extractedItems.isEmpty()) {
                extractedItems = "Room Service Amenities";
            }
            if (confirmationMessage.isEmpty()) {
                confirmationMessage = "We have received your request for: " + extractedItems + ". Our staff has been dispatched to fulfill it promptly.";
            }

            return new ServiceRequestAIResult(
                    requestType,
                    extractedItems,
                    priority,
                    estimatedMinutes,
                    confirmationMessage,
                    true // classifiedByAI
            );
        } catch (Exception e) {
            System.err.println("[ServiceRequestUnderstandingService] Failed to parse JSON: " + e.getMessage() + ". Raw output: " + rawJson);
            return null;
        }
    }

    /**
     * Deterministic rule-based item extraction fallback when Gemini is offline.
     */
    public ServiceRequestAIResult fallbackExtraction(String text, String manualType, String manualPriority) {
        String lower = (text != null) ? text.toLowerCase() : "";

        // Determine department
        String requestType = "Housekeeping";
        if (lower.contains("sandwich") || lower.contains("burger") || lower.contains("coffee") ||
            lower.contains("tea") || lower.contains("pizza") || lower.contains("food") ||
            lower.contains("breakfast") || lower.contains("dinner") || lower.contains("drink") ||
            lower.contains("wine") || lower.contains("beer") || lower.contains("snack") || lower.contains("juice")) {
            requestType = "Food Service";
        } else if (lower.contains("iron") || lower.contains("hairdryer") || lower.contains("adapter") ||
                   lower.contains("plug") || lower.contains("battery") || lower.contains("bulb") || lower.contains("remote")) {
            requestType = "Maintenance";
        } else if (lower.contains("key") || lower.contains("luggage") || lower.contains("wake") || lower.contains("checkout")) {
            requestType = "Reception";
        }

        // Determine priority
        String priority = "NORMAL";
        if (lower.contains("urgent") || lower.contains("emergency") || lower.contains("asap") ||
            lower.contains("immediately") || lower.contains("baby") || lower.contains("medicine") || lower.contains("medical")) {
            priority = "URGENT";
        } else if (lower.contains("quick") || lower.contains("soon") || lower.contains("fast") || lower.contains("hungry")) {
            priority = "HIGH";
        }

        // Extract quantified items using regex heuristics
        List<String> foundItems = new ArrayList<>();

        // Helper regex matching quantity + item
        String[][] itemPatterns = {
                {"towels?", "Towel"},
                {"bottles?\\s*(of)?\\s*water|waters?", "Water Bottle"},
                {"pillows?", "Pillow"},
                {"blankets?", "Blanket"},
                {"bedsheets?|linens?", "Bed Linen"},
                {"soaps?|shampoos?", "Toiletry Kit"},
                {"toothbrushes?|dental\\s*kits?", "Dental Kit"},
                {"coffees?|espressos?", "Coffee"},
                {"teas?", "Tea"},
                {"sandwich(es)?", "Sandwich"},
                {"burgers?", "Burger"},
                {"irons?|ironing\\s*boards?", "Iron & Board"},
                {"hangers?", "Hangers"}
        };

        for (String[] def : itemPatterns) {
            Pattern p = Pattern.compile("(?i)(?:(\\d+|one|two|three|four|five|extra)\\s+)?(" + def[0] + ")");
            Matcher m = p.matcher(lower);
            if (m.find()) {
                String qtyStr = m.group(1);
                int qty = 1;
                if (qtyStr != null) {
                    qtyStr = qtyStr.toLowerCase().trim();
                    switch (qtyStr) {
                        case "one": qty = 1; break;
                        case "two": qty = 2; break;
                        case "three": qty = 3; break;
                        case "four": qty = 4; break;
                        case "five": qty = 5; break;
                        case "extra": qty = 2; break;
                        default:
                            try { qty = Integer.parseInt(qtyStr); } catch (NumberFormatException ignored) { qty = 1; }
                    }
                }
                foundItems.add(qty + "x " + def[1]);
            }
        }

        String extractedItems;
        if (!foundItems.isEmpty()) {
            extractedItems = String.join(", ", foundItems);
        } else {
            extractedItems = (text.length() > 60) ? text.substring(0, 57) + "..." : text;
            if (extractedItems.isEmpty()) extractedItems = "General Room Service";
        }

        // Apply manual overrides
        if (manualType != null && !manualType.equalsIgnoreCase("AUTO") && !manualType.trim().isEmpty()) {
            requestType = manualType.trim();
        }
        if (manualPriority != null && !manualPriority.equalsIgnoreCase("AUTO") && !manualPriority.trim().isEmpty()) {
            priority = manualPriority.trim();
        }

        String eta = "Food Service".equals(requestType) ? "20-30 mins" : "15-20 mins";
        String confirmation = "We have received your request for: " + extractedItems + ". Our staff has been dispatched to fulfill it promptly.";

        return new ServiceRequestAIResult(
                requestType,
                extractedItems,
                priority,
                eta,
                confirmation,
                false // classifiedByAI
        );
    }
}
