package com.hotel.controller;

import com.google.gson.JsonObject;
import com.hotel.model.ChatMessage;
import com.hotel.model.User;
import com.hotel.service.HotelAssistantService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * HotelAssistantServlet - Stage 16 AI Concierge & Chatbot Controller.
 * Mapped to /assistant and /chat
 * Supports both traditional full-page form submissions and instant AJAX JSON messaging.
 */
@WebServlet(urlPatterns = {"/assistant", "/chat"})
public class HotelAssistantServlet extends HttpServlet {

    private HotelAssistantService assistantService;

    @Override
    public void init() throws ServletException {
        this.assistantService = new HotelAssistantService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);

        String action = request.getParameter("action");
        if ("clear".equalsIgnoreCase(action)) {
            session.removeAttribute("chatHistory");
            response.sendRedirect("assistant");
            return;
        }

        List<ChatMessage> history = getOrCreateChatHistory(session);
        request.setAttribute("chatHistory", history);

        request.getRequestDispatcher("assistant.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(true);

        User currentUser = (User) session.getAttribute("user");
        List<ChatMessage> history = getOrCreateChatHistory(session);

        String userQuery = request.getParameter("message");
        if (userQuery == null || userQuery.trim().isEmpty()) {
            userQuery = "";
        } else {
            userQuery = userQuery.trim();
        }

        // Add user message to history
        if (!userQuery.isEmpty()) {
            history.add(new ChatMessage("user", userQuery, false));
        }

        // Generate AI / Concierge response
        ChatMessage assistantReply = assistantService.processQuery(userQuery, currentUser, history);
        history.add(assistantReply);

        // Keep last 30 messages in memory to prevent session bloat
        if (history.size() > 30) {
            history.subList(0, history.size() - 30).clear();
        }
        session.setAttribute("chatHistory", history);

        // Handle AJAX JSON requests
        String format = request.getParameter("format");
        String acceptHeader = request.getHeader("Accept");
        boolean isAjax = "json".equalsIgnoreCase(format) ||
                (acceptHeader != null && acceptHeader.contains("application/json")) ||
                "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));

        if (isAjax) {
            response.setContentType("application/json;charset=UTF-8");
            JsonObject json = new JsonObject();
            json.addProperty("status", "success");
            json.addProperty("sender", assistantReply.getSender());
            json.addProperty("message", assistantReply.getMessage());
            json.addProperty("timestamp", assistantReply.getTimestamp());
            json.addProperty("fromAI", assistantReply.isFromAI());
            response.getWriter().write(json.toString());
        } else {
            response.sendRedirect("assistant");
        }
    }

    @SuppressWarnings("unchecked")
    private List<ChatMessage> getOrCreateChatHistory(HttpSession session) {
        List<ChatMessage> history = (List<ChatMessage>) session.getAttribute("chatHistory");
        if (history == null) {
            history = new ArrayList<>();
            history.add(new ChatMessage(
                    "assistant",
                    "✨ Hello! I am Aura, your 24/7 AI Hotel Concierge. You can ask me anything about breakfast, pool & gym timings, Wi-Fi password, checkout details, or your live booking status! How may I assist you today? 🛎️",
                    true
            ));
            session.setAttribute("chatHistory", history);
        }
        return history;
    }
}
