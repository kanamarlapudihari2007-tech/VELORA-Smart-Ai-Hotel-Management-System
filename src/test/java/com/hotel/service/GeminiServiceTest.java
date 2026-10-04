package com.hotel.service;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * GeminiServiceTest - Unit tests for Google Gemini configuration, key masking, and validation guards.
 */
public class GeminiServiceTest {

    @Test
    public void testApiKeyDetectionWhenEmpty() {
        GeminiService service = new GeminiService("", "gemini-2.5-flash");
        assertFalse("Service must report key not configured when empty", service.isApiKeyConfigured());
        assertEquals("NOT CONFIGURED", service.getMaskedApiKey());
    }

    @Test
    public void testApiKeyMasking() {
        GeminiService service = new GeminiService("AIzaSyB1234567890abcdefghijklmnopqrst", "gemini-2.5-flash");
        assertTrue("Service must detect configured API key", service.isApiKeyConfigured());
        String masked = service.getMaskedApiKey();
        assertTrue("Masked key must begin with AIza", masked.startsWith("AIza"));
        assertTrue("Masked key must end with last 4 chars", masked.endsWith("qrst"));
        assertFalse("Full key must never be displayed in logs or diagnostics", masked.contains("1234567890abcdef"));
    }

    @Test(expected = IllegalStateException.class)
    public void testCallFailsSafelyWithoutKey() throws Exception {
        GeminiService service = new GeminiService("", "gemini-2.5-flash");
        service.callGenerateContent("System prompt", "User test");
    }

    @Test
    public void testModelSelectionAndEndpoint() {
        GeminiService service = new GeminiService("AIza-mock", "gemini-2.0-flash");
        assertEquals("gemini-2.0-flash", service.getModel());
        assertEquals("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent", service.getEndpoint());
    }

    @Test
    public void testDefaultModelWhenNull() {
        GeminiService service = new GeminiService("AIza-mock", null);
        assertEquals("gemini-3.5-flash-lite", service.getModel());
        assertTrue(service.getEndpoint().contains("gemini-3.5-flash-lite:generateContent"));
    }
}
