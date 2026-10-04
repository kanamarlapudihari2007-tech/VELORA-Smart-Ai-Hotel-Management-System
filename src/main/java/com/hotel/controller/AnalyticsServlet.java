package com.hotel.controller;

import com.hotel.model.HotelAnalytics;
import com.hotel.model.User;
import com.hotel.service.OperationsAnalyticsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AnalyticsServlet - Stage 17 AI Hotel Operations Analytics & Executive Intelligence Controller.
 * Mapped to /analytics and /manager-analytics.
 * Provides management with real-time occupancy statistics, revenue metrics,
 * complaint trends, staff leaderboard, and Google Gemini automated executive briefings.
 */
@WebServlet(urlPatterns = {"/analytics", "/manager-analytics"})
public class AnalyticsServlet extends HttpServlet {

    private OperationsAnalyticsService analyticsService;

    @Override
    public void init() throws ServletException {
        this.analyticsService = new OperationsAnalyticsService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        // Check if user requested an explicit regeneration of the AI executive briefing
        String refreshParam = request.getParameter("refreshAi");
        boolean forceRefresh = "true".equalsIgnoreCase(refreshParam);

        HotelAnalytics analytics = analyticsService.getExecutiveAnalytics(forceRefresh);
        request.setAttribute("analytics", analytics);

        request.getRequestDispatcher("analytics.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
