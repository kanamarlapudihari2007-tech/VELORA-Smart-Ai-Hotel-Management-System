package com.hotel.service;

import com.hotel.model.Review;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ReviewSentimentServiceTest - Unit tests for Stage 18 AI Guest Reviews & Sentiment Analysis.
 */
public class ReviewSentimentServiceTest {

    private ReviewSentimentService service;

    @Before
    public void setUp() {
        service = new ReviewSentimentService();
    }

    @Test
    public void testPositiveReviewFallback() {
        Review review = new Review(1, 1, 5, "Amazing luxury stay! The room was spotless, the bed was very comfortable, and breakfast was delicious. Staff was super friendly!");
        service.applyFallbackHeuristics(review);

        assertEquals("POSITIVE", review.getSentiment());
        assertTrue("Sentiment score should be positive", review.getSentimentScore() > 0.5);
        assertEquals("POSITIVE", review.getAspectCleanliness());
        assertEquals("POSITIVE", review.getAspectStaff());
        assertEquals("POSITIVE", review.getAspectRoom());
        assertEquals("POSITIVE", review.getAspectFood());
        assertFalse(review.isEscalated());
        assertNotNull(review.getAiReplyDraft());
        assertTrue(review.getAiReplyDraft().contains("Thank you"));
    }

    @Test
    public void testNeutralReviewFallback() {
        Review review = new Review(2, 1, 3, "Average hotel stay. The room was okay but WiFi was slow. Food was decent.");
        service.applyFallbackHeuristics(review);

        assertEquals("NEUTRAL", review.getSentiment());
        assertEquals(0.10, review.getSentimentScore(), 0.01);
        assertFalse(review.isEscalated());
        assertNotNull(review.getAiReplyDraft());
    }

    @Test
    public void testNegativeReviewFallback() {
        Review review = new Review(3, 1, 1, "Terrible experience. The bathroom was dirty with stains and the reception staff was rude and unhelpful.");
        service.applyFallbackHeuristics(review);

        assertEquals("NEGATIVE", review.getSentiment());
        assertTrue("Sentiment score should be negative", review.getSentimentScore() < 0);
        assertEquals("NEGATIVE", review.getAspectCleanliness());
        assertEquals("NEGATIVE", review.getAspectStaff());
        assertNotNull(review.getAiReplyDraft());
    }

    @Test
    public void testCriticalEscalationFallback() {
        Review review = new Review(4, 1, 1, "Unacceptable health hazard! Found a cockroach under the bed and bedbug bites on my arm!");
        service.applyFallbackHeuristics(review);

        assertTrue("Should trigger emergency escalation", review.isEscalated());
        assertEquals("CRITICAL", review.getSentiment());
        assertEquals(-1.00, review.getSentimentScore(), 0.01);
    }
}
