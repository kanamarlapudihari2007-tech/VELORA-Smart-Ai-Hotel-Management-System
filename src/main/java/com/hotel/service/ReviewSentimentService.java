package com.hotel.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hotel.model.Review;

import java.util.Locale;

/**
 * ReviewSentimentService - Stage 18 AI Guest Reviews & Aspect-Based Sentiment Analysis.
 * Leverages Google Gemini API (gemini-3.5-flash-lite) to extract multi-aspect sentiment,
 * detect escalation risks, and draft personalized manager response letters.
 */
public class ReviewSentimentService {

    private final GeminiService geminiService;

    public ReviewSentimentService() {
        this(new GeminiService());
    }

    public ReviewSentimentService(GeminiService geminiService) {
        this.geminiService = (geminiService != null) ? geminiService : new GeminiService();
    }

    /**
     * Performs AI sentiment analysis on the guest's review and attaches
     * sentiment scores, aspect breakdowns, and an AI response draft.
     */
    public Review analyzeReview(Review review) {
        if (review == null || review.getReviewText() == null) {
            return review;
        }

        if (geminiService.isApiKeyConfigured()) {
            try {
                String systemInstruction = "You are an expert Luxury Hotel Guest Experience & Sentiment AI Analyst.\n"
                        + "Analyze the provided guest review and rating, then respond ONLY with a valid JSON object without markdown codeblocks or commentary.";
                String userPrompt = buildPrompt(review.getRating(), review.getReviewText());

                String rawAiResponse = geminiService.callGenerateContent(systemInstruction, userPrompt);
                if (rawAiResponse != null && !rawAiResponse.trim().isEmpty()) {
                    parseAndApplyAiResponse(review, rawAiResponse);
                    return review;
                }
            } catch (Exception e) {
                System.err.println("Warning: Gemini sentiment analysis call failed: " + e.getMessage());
            }
        }

        // Fallback to deterministic heuristics if AI is offline or rate-limited
        applyFallbackHeuristics(review);
        return review;
    }

    private String buildPrompt(int rating, String reviewText) {
        return "Guest Rating: " + rating + " out of 5 stars\n"
                + "Review Content: \"" + reviewText.replace("\"", "\\\"") + "\"\n\n"
                + "Respond with this exact JSON structure:\n"
                + "{\n"
                + "  \"sentiment\": \"POSITIVE\" | \"NEUTRAL\" | \"NEGATIVE\" | \"CRITICAL\",\n"
                + "  \"sentimentScore\": <float between -1.00 and 1.00>,\n"
                + "  \"aspectCleanliness\": \"POSITIVE\" | \"NEUTRAL\" | \"NEGATIVE\" | \"N/A\",\n"
                + "  \"aspectStaff\": \"POSITIVE\" | \"NEUTRAL\" | \"NEGATIVE\" | \"N/A\",\n"
                + "  \"aspectRoom\": \"POSITIVE\" | \"NEUTRAL\" | \"NEGATIVE\" | \"N/A\",\n"
                + "  \"aspectFood\": \"POSITIVE\" | \"NEUTRAL\" | \"NEGATIVE\" | \"N/A\",\n"
                + "  \"aspectValue\": \"POSITIVE\" | \"NEUTRAL\" | \"NEGATIVE\" | \"N/A\",\n"
                + "  \"keyHighlights\": \"<Brief 1-sentence summary of specific praises or complaints>\",\n"
                + "  \"isEscalated\": <true if severe health, safety, bedbugs, theft or harassment reported, otherwise false>,\n"
                + "  \"managementReplyDraft\": \"<A warm, empathetic, 2-3 sentence personalized response letter signed by General Management addressing their specific points>\"\n"
                + "}";
    }

    private void parseAndApplyAiResponse(Review review, String jsonResponse) {
        String cleanJson = jsonResponse.trim();
        if (cleanJson.startsWith("```json")) {
            cleanJson = cleanJson.substring(7);
        } else if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.substring(3);
        }
        if (cleanJson.endsWith("```")) {
            cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
        }
        cleanJson = cleanJson.trim();

        JsonObject obj = JsonParser.parseString(cleanJson).getAsJsonObject();

        if (obj.has("sentiment")) {
            review.setSentiment(obj.get("sentiment").getAsString().toUpperCase());
        }
        if (obj.has("sentimentScore")) {
            review.setSentimentScore(obj.get("sentimentScore").getAsDouble());
        }
        if (obj.has("aspectCleanliness")) {
            review.setAspectCleanliness(obj.get("aspectCleanliness").getAsString().toUpperCase());
        }
        if (obj.has("aspectStaff")) {
            review.setAspectStaff(obj.get("aspectStaff").getAsString().toUpperCase());
        }
        if (obj.has("aspectRoom")) {
            review.setAspectRoom(obj.get("aspectRoom").getAsString().toUpperCase());
        }
        if (obj.has("aspectFood")) {
            review.setAspectFood(obj.get("aspectFood").getAsString().toUpperCase());
        }
        if (obj.has("aspectValue")) {
            review.setAspectValue(obj.get("aspectValue").getAsString().toUpperCase());
        }
        if (obj.has("keyHighlights")) {
            review.setKeyHighlights(obj.get("keyHighlights").getAsString());
        }
        if (obj.has("isEscalated")) {
            review.setEscalated(obj.get("isEscalated").getAsBoolean());
        }
        if (obj.has("managementReplyDraft")) {
            review.setAiReplyDraft(obj.get("managementReplyDraft").getAsString());
        }
    }

    /**
     * Deterministic heuristic sentiment analyzer ensuring instant, robust operation.
     */
    public void applyFallbackHeuristics(Review review) {
        String text = review.getReviewText() != null ? review.getReviewText().toLowerCase(Locale.ROOT) : "";
        int rating = review.getRating();

        // 1. Overall Sentiment & Score
        if (rating >= 4) {
            review.setSentiment("POSITIVE");
            review.setSentimentScore(rating == 5 ? 0.90 : 0.65);
        } else if (rating == 3) {
            review.setSentiment("NEUTRAL");
            review.setSentimentScore(0.10);
        } else {
            review.setSentiment("NEGATIVE");
            review.setSentimentScore(rating == 1 ? -0.85 : -0.50);
        }

        // Escalation triggers
        boolean escalated = text.contains("cockroach") || text.contains("bedbug") || text.contains("theft")
                || text.contains("stolen") || text.contains("poison") || text.contains("harass")
                || text.contains("police") || text.contains("injury") || text.contains("dirty blood");
        review.setEscalated(escalated);
        if (escalated) {
            review.setSentiment("CRITICAL");
            review.setSentimentScore(-1.00);
        }

        // 2. Aspect Breakdown
        review.setAspectCleanliness(determineAspect(text, "clean", "spotless", "neat", "hygienic", "dirty", "smell", "dusty", "stain"));
        review.setAspectStaff(determineAspect(text, "staff", "friendly", "polite", "helpful", "reception", "rude", "slow", "unhelpful"));
        review.setAspectRoom(determineAspect(text, "bed", "room", "comfortable", "spacious", "view", "ac", "noisy", "small", "cramped"));
        review.setAspectFood(determineAspect(text, "food", "breakfast", "dinner", "delicious", "tasty", "cold", "bland", "overpriced"));
        review.setAspectValue(determineAspect(text, "value", "worth", "affordable", "price", "expensive", "rip-off", "overpriced"));

        // 3. Key Highlights
        if (rating >= 4) {
            review.setKeyHighlights("Guest commended their overall experience and hospitality with a " + rating + "-star rating.");
            review.setAiReplyDraft("Thank you so much for your generous review! We are delighted that you had a wonderful stay with us, and our entire team looks forward to welcoming you back to AI Smart Hotel very soon.");
        } else if (rating == 3) {
            review.setKeyHighlights("Guest provided mixed feedback regarding their stay with average satisfaction.");
            review.setAiReplyDraft("Thank you for your constructive feedback. We are pleased you stayed with us, but regret that certain aspects did not fully exceed your expectations. We will share your notes with our team to refine our services.");
        } else {
            review.setKeyHighlights("Guest reported dissatisfaction during their stay requiring operational review.");
            review.setAiReplyDraft("We sincerely apologize that your experience did not meet our standard of hospitality. We take your feedback very seriously, and our management team is investigating your concerns immediately to ensure corrective actions are taken.");
        }
    }

    private String determineAspect(String text, String... keywords) {
        boolean hasMention = false;
        int positiveHits = 0;
        int negativeHits = 0;

        String[] positiveWords = {"good", "great", "excellent", "amazing", "clean", "spotless", "friendly", "helpful", "comfortable", "delicious", "tasty", "worth", "spacious"};
        String[] negativeWords = {"bad", "dirty", "smell", "rude", "noisy", "slow", "cold", "bland", "expensive", "unhelpful", "stain", "broken", "terrible"};

        for (String kw : keywords) {
            if (text.contains(kw)) {
                hasMention = true;
                break;
            }
        }

        if (!hasMention) {
            return "N/A";
        }

        for (String pw : positiveWords) {
            if (text.contains(pw)) positiveHits++;
        }
        for (String nw : negativeWords) {
            if (text.contains(nw)) negativeHits++;
        }

        if (negativeHits > positiveHits) {
            return "NEGATIVE";
        } else if (positiveHits > 0) {
            return "POSITIVE";
        } else {
            return "NEUTRAL";
        }
    }
}
