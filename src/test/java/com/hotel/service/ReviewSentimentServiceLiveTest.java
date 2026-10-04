package com.hotel.service;

import com.hotel.model.Review;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ReviewSentimentServiceLiveTest - Live integration test verifying real Google Gemini API
 * sentiment analysis, multi-aspect mining, and personalized response drafting.
 */
public class ReviewSentimentServiceLiveTest {

    @Test
    public void testLiveGeminiReviewSentimentAnalysis() {
        ReviewSentimentService service = new ReviewSentimentService();

        Review review = new Review(
            1, 1, 5,
            "We had an unforgettable anniversary weekend! The Deluxe Suite was impeccably clean with breathtaking skyline views. "
            + "Sarah at reception gave us a warm welcome and complimentary champagne. The rooftop breakfast was tasty, though checkout had a slight 5-minute wait."
        );

        long startTime = System.currentTimeMillis();
        Review result = service.analyzeReview(review);
        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("==================================================");
        System.out.println("STAGE 18 LIVE GEMINI REVIEW SENTIMENT ANALYSIS:");
        System.out.println("Elapsed Time       : " + elapsed + " ms");
        System.out.println("Sentiment          : " + result.getSentiment());
        System.out.println("Sentiment Score    : " + result.getSentimentScore());
        System.out.println("Cleanliness Aspect : " + result.getAspectCleanliness());
        System.out.println("Staff Aspect       : " + result.getAspectStaff());
        System.out.println("Room Aspect        : " + result.getAspectRoom());
        System.out.println("Food Aspect        : " + result.getAspectFood());
        System.out.println("Value Aspect       : " + result.getAspectValue());
        System.out.println("Key Highlights     : " + result.getKeyHighlights());
        System.out.println("Escalated          : " + result.isEscalated());
        System.out.println("Manager Reply Draft: " + result.getAiReplyDraft());
        System.out.println("==================================================");

        assertNotNull("Sentiment should not be null", result.getSentiment());
        assertTrue("Sentiment should be POSITIVE", "POSITIVE".equalsIgnoreCase(result.getSentiment()));
        assertNotNull("AI Reply draft should be populated", result.getAiReplyDraft());
        assertTrue("Reply draft should not be empty", result.getAiReplyDraft().length() > 20);
        assertFalse("Should not be escalated", result.isEscalated());
    }
}
