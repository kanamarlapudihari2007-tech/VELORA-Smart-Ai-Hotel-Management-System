package com.hotel.service;

import com.hotel.model.ChatMessage;
import com.hotel.model.User;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Live integration test for HotelAssistantService (Stage 16).
 * Connects to Google Gemini API to test natural language concierge responses.
 */
public class HotelAssistantServiceLiveTest {

    private HotelAssistantService service;
    private GeminiService geminiService;

    @Before
    public void setUp() {
        geminiService = new GeminiService();
        Assume.assumeTrue("Gemini API key must be configured to run live integration test",
                geminiService.isApiKeyConfigured());
        service = new HotelAssistantService(geminiService, null, null, null);
    }

    @Test
    public void testLiveGemini_ConciergeQuestion() {
        User guest = new User();
        guest.setUsername("alex_guest");
        guest.setEmail("alex@gmail.com");
        guest.setRole("GUEST");

        List<ChatMessage> history = new ArrayList<>();
        history.add(new ChatMessage("user", "Hello Aura, what time is breakfast and where is the pool located?", false));

        ChatMessage reply = service.processQuery(
                "Hello Aura, what time is breakfast and where is the pool located?",
                guest,
                new ArrayList<>()
        );

        assertNotNull("Assistant reply should not be null", reply);
        assertEquals("assistant", reply.getSender());

        System.out.println("--------------------------------------------------");
        System.out.println("LIVE GEMINI CONCIERGE RESPONSE:");
        System.out.println("Sender      : " + reply.getSender());
        System.out.println("Reply       : " + reply.getMessage());
        System.out.println("Timestamp   : " + reply.getTimestamp());
        System.out.println("From AI     : " + reply.isFromAI());
        System.out.println("--------------------------------------------------");

        assertTrue("Should be processed by Gemini AI", reply.isFromAI());
        assertFalse("Reply message should not be empty", reply.getMessage().trim().isEmpty());
    }
}
