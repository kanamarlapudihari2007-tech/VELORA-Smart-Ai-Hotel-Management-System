package com.hotel.service;

import com.hotel.model.ChatMessage;
import com.hotel.model.User;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Unit tests for HotelAssistantService (Stage 16).
 * Validates fallback concierge FAQ matching, live DB context builder, and null safety.
 */
public class HotelAssistantServiceTest {

    private HotelAssistantService service;

    @Before
    public void setUp() {
        // Test fallback engine with offline Gemini service
        service = new HotelAssistantService(
                new GeminiService() {
                    @Override
                    public boolean isApiKeyConfigured() {
                        return false;
                    }
                },
                null, null, null
        );
    }

    @Test
    public void testEmptyQueryHandling() {
        ChatMessage reply = service.processQuery("", null, new ArrayList<>());
        assertNotNull(reply);
        assertEquals("assistant", reply.getSender());
        assertTrue(reply.getMessage().contains("assist you"));
    }

    @Test
    public void testBreakfastFallback() {
        ChatMessage reply = service.processQuery("What time is breakfast served?", null, new ArrayList<>());
        assertNotNull(reply);
        assertFalse(reply.isFromAI());
        assertTrue(reply.getMessage().contains("7:00 AM"));
        assertTrue(reply.getMessage().contains("Grand Dining Room"));
    }

    @Test
    public void testPoolFallback() {
        ChatMessage reply = service.processQuery("Where is the swimming pool?", null, new ArrayList<>());
        assertNotNull(reply);
        assertTrue(reply.getMessage().contains("5th floor"));
        assertTrue(reply.getMessage().contains("6:00 AM"));
    }

    @Test
    public void testGymFallback() {
        ChatMessage reply = service.processQuery("Is there a fitness center or gym?", null, new ArrayList<>());
        assertNotNull(reply);
        assertTrue(reply.getMessage().contains("3rd floor"));
        assertTrue(reply.getMessage().contains("24/7"));
    }

    @Test
    public void testWiFiFallback() {
        ChatMessage reply = service.processQuery("What is the wifi password?", null, new ArrayList<>());
        assertNotNull(reply);
        assertTrue(reply.getMessage().contains("SmartHotel_Guest_HighSpeed"));
        assertTrue(reply.getMessage().contains("welcome_guest"));
    }

    @Test
    public void testCheckoutFallback() {
        ChatMessage reply = service.processQuery("When do I need to check out?", null, new ArrayList<>());
        assertNotNull(reply);
        assertTrue(reply.getMessage().contains("11:00 AM"));
    }

    @Test
    public void testLiveContextBuilder_NullUser() {
        String context = service.buildLiveContext(null);
        assertNotNull(context);
        assertTrue(context.contains("unauthenticated"));
    }

    @Test
    public void testLiveContextBuilder_StaffUser() {
        User staff = new User();
        staff.setUsername("mike_maintenance");
        staff.setRole("STAFF");
        staff.setEmail("mike@smarthotel.com");

        String context = service.buildLiveContext(staff);
        assertNotNull(context);
        assertTrue(context.contains("STAFF"));
    }
}
