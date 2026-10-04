package com.hotel.service;

import com.hotel.model.ComplaintClassificationResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ComplaintClassificationServiceTest - Unit tests for Stage 14 AI complaint classification and fallback heuristics.
 */
public class ComplaintClassificationServiceTest {

    private ComplaintClassificationService service;

    @Before
    public void setUp() {
        // Instantiate with dummy mock key to test deterministic fallback paths safely
        GeminiService mockGemini = new GeminiService("", "gemini-flash-latest");
        service = new ComplaintClassificationService(mockGemini);
    }

    @Test
    public void testHousekeepingFallbackDetection() {
        String complaint = "The bathroom has no fresh towels and the bed sheets are stained.";
        ComplaintClassificationResult result = service.classifyComplaint(complaint, "AUTO", "AUTO");

        assertNotNull(result);
        assertEquals("Housekeeping", result.getCategory());
        assertNotNull(result.getReassuranceMessage());
        assertFalse("Should use heuristic fallback when API key is empty in test", result.isClassifiedByAI());
    }

    @Test
    public void testMaintenanceUrgentFallbackDetection() {
        String complaint = "Water is flooding rapidly from the bathroom pipe and sparks are coming from the outlet!";
        ComplaintClassificationResult result = service.classifyComplaint(complaint, "AUTO", "AUTO");

        assertNotNull(result);
        assertEquals("Maintenance", result.getCategory());
        assertEquals("URGENT", result.getPriority());
    }

    @Test
    public void testSecurityFallbackDetection() {
        String complaint = "There is a very loud party and screaming noise next door at 2 AM.";
        ComplaintClassificationResult result = service.classifyComplaint(complaint, "AUTO", "AUTO");

        assertNotNull(result);
        assertEquals("Security", result.getCategory());
    }

    @Test
    public void testManualOverrideHonored() {
        String complaint = "Everything is great but I need extra water.";
        ComplaintClassificationResult result = service.classifyComplaint(complaint, "Food Service", "HIGH");

        assertNotNull(result);
        assertEquals("Food Service", result.getCategory());
        assertEquals("HIGH", result.getPriority());
    }
}
