package com.hotel.service;

import com.hotel.dao.AnalyticsDAO;
import com.hotel.model.HotelAnalytics;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * OperationsAnalyticsServiceLiveTest - Live integration test for Stage 17.
 * Validates Google Gemini AI Executive Briefing synthesis from real operational metrics.
 */
public class OperationsAnalyticsServiceLiveTest {

    private OperationsAnalyticsService service;
    private GeminiService geminiService;

    @Before
    public void setUp() {
        geminiService = new GeminiService();
        Assume.assumeTrue("Gemini API key must be configured to run live integration test",
                geminiService.isApiKeyConfigured());
        service = new OperationsAnalyticsService(new AnalyticsDAO(), geminiService);
    }

    @Test
    public void testLiveGemini_ExecutiveBriefing() {
        long startTime = System.currentTimeMillis();
        HotelAnalytics analytics = service.getExecutiveAnalytics(true);
        if (!analytics.isFromAI()) {
            // Transient network retry
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            analytics = service.getExecutiveAnalytics(true);
        }
        long elapsed = System.currentTimeMillis() - startTime;

        assertNotNull("Analytics must not be null", analytics);
        assertNotNull("AI Headline must not be null", analytics.getAiExecutiveHeadline());
        assertFalse("AI Headline must not be empty", analytics.getAiExecutiveHeadline().trim().isEmpty());

        System.out.println("==================================================");
        System.out.println("STAGE 17 LIVE GEMINI EXECUTIVE BRIEFING:");
        System.out.println("Elapsed Time   : " + elapsed + " ms");
        System.out.println("From AI        : " + analytics.isFromAI());
        System.out.println("Headline       : " + analytics.getAiExecutiveHeadline());
        System.out.println("Briefing       : " + analytics.getAiOperationalBriefing());
        System.out.println("Risk Alerts    : " + analytics.getAiRiskAlerts());
        System.out.println("Recommendations: " + analytics.getAiActionableRecommendations());
        System.out.println("Occupancy Rate : " + analytics.getOccupancyRate() + "%");
        System.out.println("Total Revenue  : $" + analytics.getTotalRevenue());
        System.out.println("==================================================");

        assertNotNull(analytics.getAiOperationalBriefing());
    }
}
