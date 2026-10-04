package com.hotel.service;

import com.hotel.dao.AnalyticsDAO;
import com.hotel.model.HotelAnalytics;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * OperationsAnalyticsServiceTest - Unit tests for Stage 17 AI Operations Analytics & Briefings.
 * Validates data aggregation, heuristic briefing fallback, and in-memory briefing caching.
 */
public class OperationsAnalyticsServiceTest {

    private OperationsAnalyticsService service;

    @Before
    public void setUp() {
        // Offline Gemini service to test deterministic heuristic briefing
        GeminiService offlineGemini = new GeminiService() {
            @Override
            public boolean isApiKeyConfigured() {
                return false;
            }
        };

        service = new OperationsAnalyticsService(new AnalyticsDAO(), offlineGemini);
    }

    @Test
    public void testGetExecutiveAnalytics_NotNull() {
        HotelAnalytics analytics = service.getExecutiveAnalytics(true);
        assertNotNull("Analytics instance must not be null", analytics);
        assertTrue("Total rooms must be >= 0", analytics.getTotalRooms() >= 0);
        assertTrue("Occupancy rate must be >= 0.0", analytics.getOccupancyRate() >= 0.0);
    }

    @Test
    public void testHeuristicBriefing_Populated() {
        HotelAnalytics analytics = service.getExecutiveAnalytics(true);
        assertNotNull("AI executive headline must not be null", analytics.getAiExecutiveHeadline());
        assertFalse("Headline must not be empty", analytics.getAiExecutiveHeadline().trim().isEmpty());
        assertNotNull("Operational briefing must not be null", analytics.getAiOperationalBriefing());
        assertNotNull("Risk alerts must not be null", analytics.getAiRiskAlerts());
        assertNotNull("Actionable recommendations must not be null", analytics.getAiActionableRecommendations());
        assertFalse("Should indicate heuristic fallback when offline", analytics.isFromAI());
    }

    @Test
    public void testBriefingCaching() {
        HotelAnalytics first = service.getExecutiveAnalytics(true);
        String firstHeadline = first.getAiExecutiveHeadline();

        // Second call without force refresh should return cached briefing immediately
        HotelAnalytics second = service.getExecutiveAnalytics(false);
        assertEquals("Cached headline must match first call", firstHeadline, second.getAiExecutiveHeadline());
    }
}
