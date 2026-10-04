package com.hotel.service;

import com.hotel.model.ServiceRequestAIResult;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Live integration test for ServiceRequestUnderstandingService (Stage 15).
 * Connects to Google Gemini API to test real natural language item extraction.
 * Automatically skipped if GEMINI_API_KEY is not configured.
 */
public class ServiceRequestUnderstandingServiceLiveTest {

    private ServiceRequestUnderstandingService service;
    private GeminiService geminiService;

    @Before
    public void setUp() {
        geminiService = new GeminiService();
        Assume.assumeTrue("Gemini API key must be configured to run live integration test",
                geminiService.isApiKeyConfigured());
        service = new ServiceRequestUnderstandingService(geminiService);
    }

    @Test
    public void testLiveGemini_ServiceRequestUnderstanding() {
        String guestPrompt = "Can you please bring 2 extra bath towels, 1 bottle of sparkling water, and 2 feather pillows to room 201 as soon as possible?";

        ServiceRequestAIResult result = service.analyzeRequest(guestPrompt, "AUTO", "AUTO");

        assertNotNull("Result should not be null", result);

        System.out.println("--------------------------------------------------");
        System.out.println("LIVE GEMINI SERVICE-REQUEST EXTRACTION RESULT:");
        System.out.println("Department       : " + result.getRequestType());
        System.out.println("Extracted Items  : " + result.getExtractedItems());
        System.out.println("Priority         : " + result.getPriority());
        System.out.println("Estimated Time   : " + result.getEstimatedMinutes());
        System.out.println("Confirmation Msg : " + result.getConfirmationMessage());
        System.out.println("Classified By AI : " + result.isClassifiedByAI());
        System.out.println("--------------------------------------------------");

        assertTrue("Should be processed by Gemini AI", result.isClassifiedByAI());
        assertNotNull("Department should be assigned", result.getRequestType());
        assertFalse("Extracted items should not be empty", result.getExtractedItems().isEmpty());
        assertNotNull("Priority should be assigned", result.getPriority());
        assertNotNull("Estimated minutes should be populated", result.getEstimatedMinutes());
        assertNotNull("Confirmation message should be populated", result.getConfirmationMessage());
    }
}
