package com.hotel.controller;

import com.hotel.dao.RoomDAO;
import com.hotel.model.DynamicPricingRecommendation;
import com.hotel.model.RevenueOptimizationReport;
import com.hotel.model.User;
import com.hotel.service.DynamicPricingService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * DynamicPricingServlet - Stage 19 AI Dynamic Room Pricing & Revenue Optimization Controller.
 * Handles /dynamic-pricing and /manager/dynamic-pricing endpoints.
 */
@WebServlet(urlPatterns = {"/dynamic-pricing", "/manager/dynamic-pricing", "/apply-pricing"})
public class DynamicPricingServlet extends HttpServlet {

    private DynamicPricingService pricingService;
    private RoomDAO roomDAO;

    @Override
    public void init() throws ServletException {
        pricingService = new DynamicPricingService();
        roomDAO = new RoomDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"MANAGER".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect("dashboard?error=forbidden");
            return;
        }

        boolean forceRefresh = "true".equalsIgnoreCase(request.getParameter("refresh"));
        RevenueOptimizationReport report = pricingService.generateReport(forceRefresh);

        request.setAttribute("report", report);
        request.getRequestDispatcher("dynamic-pricing.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!"MANAGER".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect("dashboard?error=forbidden");
            return;
        }

        String action = request.getParameter("action");

        if ("apply".equalsIgnoreCase(action) || "apply_all".equalsIgnoreCase(action)) {
            // Apply recommended dynamic rates to MySQL room_types table
            RevenueOptimizationReport report = pricingService.generateReport(false);
            if (report != null && report.getRecommendations() != null) {
                for (DynamicPricingRecommendation rec : report.getRecommendations()) {
                    String overrideParam = request.getParameter("price_" + rec.getRoomTypeId());
                    BigDecimal priceToApply = rec.getRecommendedPrice();

                    if (overrideParam != null && !overrideParam.trim().isEmpty()) {
                        try {
                            priceToApply = new BigDecimal(overrideParam.trim());
                        } catch (NumberFormatException ignored) {}
                    }

                    if (priceToApply != null && priceToApply.compareTo(BigDecimal.ZERO) > 0) {
                        roomDAO.updateRoomTypePrice(rec.getRoomTypeId(), priceToApply);
                    }
                }
            }
            pricingService.generateReport(true); // refresh report with new base rates
            response.sendRedirect(request.getContextPath() + "/dynamic-pricing?msg=applied_all");
            return;

        } else if ("apply_single".equalsIgnoreCase(action)) {
            String roomTypeIdStr = request.getParameter("roomTypeId");
            String newPriceStr = request.getParameter("newPrice");

            if (roomTypeIdStr != null && newPriceStr != null) {
                try {
                    int roomTypeId = Integer.parseInt(roomTypeIdStr.trim());
                    BigDecimal priceToApply = new BigDecimal(newPriceStr.trim());
                    if (priceToApply.compareTo(BigDecimal.ZERO) > 0) {
                        roomDAO.updateRoomTypePrice(roomTypeId, priceToApply);
                    }
                } catch (Exception ignored) {}
            }
            pricingService.generateReport(true);
            response.sendRedirect(request.getContextPath() + "/dynamic-pricing?msg=applied_single");
            return;

        } else if ("reset".equalsIgnoreCase(action)) {
            // Reset to standard baseline rates
            roomDAO.resetDefaultRoomPrices();
            pricingService.generateReport(true);
            response.sendRedirect(request.getContextPath() + "/dynamic-pricing?msg=prices_reset");
            return;

        } else if ("simulate".equalsIgnoreCase(action)) {
            // Re-run AI simulation
            pricingService.generateReport(true);
            response.sendRedirect(request.getContextPath() + "/dynamic-pricing?msg=simulation_updated");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/dynamic-pricing");
    }
}
