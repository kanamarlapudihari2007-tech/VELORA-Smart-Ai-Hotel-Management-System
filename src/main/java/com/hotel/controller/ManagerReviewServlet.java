package com.hotel.controller;

import com.hotel.dao.ReviewDAO;
import com.hotel.model.Review;
import com.hotel.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * ManagerReviewServlet - Executive Hotel Reputation & Sentiment Intelligence Controller.
 * Handles /manager-reviews and /manager/reviews endpoints.
 */
@WebServlet(urlPatterns = {"/manager-reviews", "/manager/reviews"})
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
                reviewDAO.updateManagerReply(reviewId, managerReply.trim());
                response.sendRedirect("manager-reviews?msg=reply_published");
                return;
            } catch (NumberFormatException ignored) {}
        }

        response.sendRedirect("manager-reviews?error=invalid_reply");
    }
}
