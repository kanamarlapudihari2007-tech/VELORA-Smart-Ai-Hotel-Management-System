package com.hotel.controller;

import com.hotel.dao.NotificationDAO;
import com.hotel.dao.ReviewDAO;
import com.hotel.model.Review;
import com.hotel.model.User;
import com.hotel.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * ManagerReviewServlet - Executive Hotel Reputation & Sentiment Intelligence Controller.
 * Handles /manager-reviews, /manager/reviews, and /manager-reply-review endpoints.
 */
@WebServlet(urlPatterns = {"/manager-reviews", "/manager/reviews", "/manager-reply-review"})
public class ManagerReviewServlet extends HttpServlet {

    private ReviewDAO reviewDAO;

    @Override
    public void init() throws ServletException {
        reviewDAO = new ReviewDAO();
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

        Map<String, Object> metrics = reviewDAO.getSentimentMetrics();
        List<Review> allReviews = reviewDAO.getAllReviews();

        request.setAttribute("metrics", metrics);
        request.setAttribute("reviews", allReviews);

        request.getRequestDispatcher("manager-reviews.jsp").forward(request, response);
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

        String reviewIdStr = request.getParameter("reviewId");
        String managerReply = request.getParameter("managerReply");

        if (reviewIdStr != null && managerReply != null && !managerReply.trim().isEmpty()) {
            try {
                int reviewId = Integer.parseInt(reviewIdStr.trim());
                boolean updated = reviewDAO.updateManagerReply(reviewId, managerReply.trim());
                if (updated) {
                    notifyGuestOfReply(reviewId);
                }
                response.sendRedirect(request.getContextPath() + "/manager-reviews?msg=reply_published");
                return;
            } catch (NumberFormatException ignored) {}
        }

        response.sendRedirect(request.getContextPath() + "/manager-reviews?error=invalid_reply");
    }

    private void notifyGuestOfReply(int reviewId) {
        String sql = "SELECT g.user_id, b.booking_code FROM reviews r "
                   + "JOIN guests g ON r.guest_id = g.id "
                   + "JOIN bookings b ON r.booking_id = b.id "
                   + "WHERE r.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String bookingCode = rs.getString("booking_code");
                    NotificationDAO notifDAO = new NotificationDAO();
                    notifDAO.createNotification(userId, "Official Reply to Your Review",
                            "Hotel Management has published an official response to your review for stay " + bookingCode + ".");
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not create notification for guest on manager reply: " + e.getMessage());
        }
    }
}
