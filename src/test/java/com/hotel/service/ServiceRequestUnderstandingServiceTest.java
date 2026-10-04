package com.hotel.service;

import com.hotel.model.ServiceRequestAIResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ServiceRequestUnderstandingService (Stage 15).
 * Tests heuristic fallback item extraction, department classification, and priority overrides.
 */
public class ServiceRequestUnderstandingServiceTest {

    private ServiceRequestUnderstandingService service;

    @Before
    public void setUp() {
        // Instantiate with dummy/offline service to test deterministic fallback paths
        service = new ServiceRequestUnderstandingService(new GeminiService() {
            @Override
            public boolean isApiKeyConfigured() {
                return false;
            }
        });
    }

    @Test
    public void testFallbackExtraction_HousekeepingItems() {
        String text = "Please bring 2 towels and 1 bottle of water to our room";
        ServiceRequestAIResult result = service.analyzeRequest(text, "AUTO", "AUTO");

        assertNotNull("Result should not be null", result);
        assertFalse("Should use heuristic fallback when API key is not configured", result.isClassifiedByAI());
        assertEquals("Should classify as Housekeeping", "Housekeeping", result.getRequestType());
        assertTrue("Extracted items should contain Towel", result.getExtractedItems().contains("Towel"));
        assertTrue("Extracted items should contain Water", result.getExtractedItems().contains("Water"));
        assertNotNull("Confirmation message should not be null", result.getConfirmationMessage());
        assertNotNull("Estimated ETA should not be null", result.getEstimatedMinutes());
    }

    @Test
    public void testFallbackExtraction_FoodService() {
        String text = "We would like to order 2 sandwiches and 1 hot coffee please";
        ServiceRequestAIResult result = service.analyzeRequest(text, "AUTO", "AUTO");

        assertNotNull(result);
        assertEquals("Should detect Food Service", "Food Service", result.getRequestType());
        assertTrue("Should extract sandwiches", result.getExtractedItems().contains("Sandwich"));
        assertTrue("Should extract coffee", result.getExtractedItems().contains("Coffee"));
    }

    @Test
    public void testFallbackExtraction_MaintenanceAmenities() {
        String text = "Can you send an iron and ironing board to room 201?";
        ServiceRequestAIResult result = service.analyzeRequest(text, "AUTO", "AUTO");

        assertNotNull(result);
        assertEquals("Should detect Maintenance", "Maintenance", result.getRequestType());
        assertTrue("Should extract Iron", result.getExtractedItems().contains("Iron"));
    }

    @Test
    public void testFallbackExtraction_UrgentPriorityDetection() {
        String text = "Emergency! The baby needs warm milk and medicine immediately, please help asap!";
        ServiceRequestAIResult result = service.analyzeRequest(text, "AUTO", "AUTO");

        assertNotNull(result);
        assertEquals("Priority should be URGENT", "URGENT", result.getPriority());
    }

    @Test
    public void testManualOverrides() {
        String text = "Please send extra pillows";
        ServiceRequestAIResult result = service.analyzeRequest(text, "Maintenance", "HIGH");

        assertNotNull(result);
        assertEquals("Manual category override should be respected", "Maintenance", result.getRequestType());
        assertEquals("Manual priority override should be respected", "HIGH", result.getPriority());
    }
}
