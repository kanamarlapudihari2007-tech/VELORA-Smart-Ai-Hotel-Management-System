package com.hotel.service;

import com.hotel.model.ComplaintClassificationResult;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ComplaintClassificationServiceLiveTest - Live integration test calling Google Gemini API to classify complaints.
 */
public class ComplaintClassificationServiceLiveTest {

    @Test
    public void testLiveClassificationWithGemini() {
        GeminiService gemini = new GeminiService();
        if (!gemini.isApiKeyConfigured()) {
            System.out.println("Skipping live test: GEMINI_API_KEY not configured");
            return;
        }

        ComplaintClassificationService service = new ComplaintClassificationService(gemini);
        String text = "The air conditioner in room 201 is leaking water heavily on the carpet and making a loud rattling sound, please fix it immediately!";
        ComplaintClassificationResult result = service.classifyComplaint(text, "AUTO", "AUTO");

        System.out.println("--------------------------------------------------");
        System.out.println("LIVE GEMINI CLASSIFICATION RESULT:");
        System.out.println("Category        : " + result.getCategory());
        System.out.println("Priority        : " + result.getPriority());
        System.out.println("Short Summary   : " + result.getShortDescription());
        System.out.println("Reassurance Msg : " + result.getReassuranceMessage());
        System.out.println("Classified By AI: " + result.isClassifiedByAI());
        System.out.println("--------------------------------------------------");

        assertNotNull(result);
        assertEquals("Maintenance", result.getCategory());
        assertTrue("Expected classification by live Gemini AI", result.isClassifiedByAI());
    }
}
