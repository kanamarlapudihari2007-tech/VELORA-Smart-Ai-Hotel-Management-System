package com.hotel.controller;

import com.hotel.service.GeminiService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AITestServlet - Stage 13 Verification Servlet.
 * Tests Google Gemini API connectivity, validates credentials, and performs test content generation.
 * Mapped to /ai-test
 */
@WebServlet("/ai-test")
public class AITestServlet extends HttpServlet {

    private GeminiService geminiService;

    @Override
    public void init() throws ServletException {
        geminiService = new GeminiService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Re-read service to catch dynamic environment/property updates
        geminiService = new GeminiService();

        boolean isConfigured = geminiService.isApiKeyConfigured();
        String maskedKey = geminiService.getMaskedApiKey();
        String model = geminiService.getModel();
        String endpoint = geminiService.getEndpoint();

        request.setAttribute("isConfigured", isConfigured);
        request.setAttribute("maskedKey", maskedKey);
        request.setAttribute("model", model);
        request.setAttribute("endpoint", endpoint);

        String testPrompt = request.getParameter("prompt");
        if (testPrompt != null && !testPrompt.trim().isEmpty()) {
            long startTime = System.currentTimeMillis();
            try {
                String systemInstruction = "You are an intelligent AI Hotel Concierge assistant for the AI Smart Hotel Management System. "
                        + "Provide polite, helpful, and concise information in 2-3 sentences regarding hotel rooms, housekeeping, dining, and amenities.";
                String aiReply = geminiService.callGenerateContent(systemInstruction, testPrompt.trim());
                long latency = System.currentTimeMillis() - startTime;
                request.setAttribute("testResult", aiReply);
                request.setAttribute("latency", latency);
                request.setAttribute("status", "SUCCESS");
            } catch (Exception e) {
                request.setAttribute("error", e.getMessage());
                request.setAttribute("status", "FAILED");
            }
        }

        request.getRequestDispatcher("ai-test.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
