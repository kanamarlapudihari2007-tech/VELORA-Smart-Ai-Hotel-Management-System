package com.hotel.controller;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.ReviewDAO;
import com.hotel.model.Booking;
import com.hotel.model.Review;
import com.hotel.model.User;
import com.hotel.service.ReviewSentimentService;

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
 * ReviewServlet - Guest Review Submission & AI Sentiment Evaluation Controller.
 * Handles /review, /reviews, and /submit-review endpoints.
 */
@WebServlet(urlPatterns = {"/review", "/reviews", "/submit-review"})
public class ReviewServlet extends HttpServlet {

    private BookingDAO bookingDAO;
    private ReviewDAO reviewDAO;
    private ReviewSentimentService sentimentService;

    @Override
    public void init() throws ServletException {
        bookingDAO = new BookingDAO();
        reviewDAO = new ReviewDAO();
        sentimentService = new ReviewSentimentService();
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
        if ("MANAGER".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect("manager-reviews");
            return;
        }

        int guestId = bookingDAO.getGuestIdByUserId(user.getId());
        List<Booking> allGuestBookings = bookingDAO.getBookingsByGuestId(guestId);

        // Filter bookings eligible for review: only after service utilization (CHECKED_OUT)
        List<Booking> eligibleBookings = new ArrayList<>();
        if (allGuestBookings != null) {
            for (Booking b : allGuestBookings) {
                if ("CHECKED_OUT".equalsIgnoreCase(b.getStatus())) {
                    eligibleBookings.add(b);
                }
            }
        }

        String bookingIdParam = request.getParameter("bookingId");
        Booking selectedBooking = null;
        Review existingReview = null;

        if (bookingIdParam != null && !bookingIdParam.trim().isEmpty()) {
            try {
                int bookingId = Integer.parseInt(bookingIdParam.trim());
                Booking b = bookingDAO.getBookingById(bookingId);
                if (b != null && b.getGuestId() == guestId) {
                    selectedBooking = b;
                    existingReview = reviewDAO.getReviewByBookingId(bookingId);
                }
            } catch (NumberFormatException ignored) {}
        }

        // If no booking explicitly targeted, prioritize unreviewed utilized bookings first
        if (selectedBooking == null && !eligibleBookings.isEmpty()) {
            for (Booking b : eligibleBookings) {
                if (reviewDAO.getReviewByBookingId(b.getId()) == null) {
                    selectedBooking = b;
                    break;
                }
            }
            if (selectedBooking == null) {
                selectedBooking = eligibleBookings.get(0);
            }
            existingReview = reviewDAO.getReviewByBookingId(selectedBooking.getId());
        }

        List<Review> myPastReviews = reviewDAO.getReviewsByGuestId(guestId);

        request.setAttribute("selectedBooking", selectedBooking);
        request.setAttribute("existingReview", existingReview);
        request.setAttribute("eligibleBookings", eligibleBookings);
        request.setAttribute("myPastReviews", myPastReviews);

        request.getRequestDispatcher("review.jsp").forward(request, response);
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
        int guestId = bookingDAO.getGuestIdByUserId(user.getId());

        String bookingIdStr = request.getParameter("bookingId");
        String ratingStr = request.getParameter("rating");
        String reviewText = request.getParameter("reviewText");

        if (bookingIdStr == null || ratingStr == null || reviewText == null || reviewText.trim().isEmpty()) {
            response.sendRedirect("review?error=missing_fields");
            return;
        }

        try {
            int bookingId = Integer.parseInt(bookingIdStr.trim());
            int rating = Integer.parseInt(ratingStr.trim());

            if (rating < 1) rating = 1;
            if (rating > 5) rating = 5;

            // Security check: ensure guest owns this booking
            Booking b = bookingDAO.getBookingById(bookingId);
            if (b == null || b.getGuestId() != guestId) {
                response.sendRedirect("review?error=unauthorized_booking");
                return;
            }

            // Stays can only be reviewed after service utilization is completed (CHECKED_OUT)
            if (!"CHECKED_OUT".equalsIgnoreCase(b.getStatus())) {
                response.sendRedirect("review?bookingId=" + bookingId + "&error=not_utilized");
                return;
            }

            // Reviews are immutable: a user CANNOT update a review once submitted
            if (reviewDAO.getReviewByBookingId(bookingId) != null) {
                response.sendRedirect("review?bookingId=" + bookingId + "&error=already_reviewed");
                return;
            }

            // Construct New Review & Run Google Gemini Sentiment Analysis
            Review review = new Review(bookingId, guestId, rating, reviewText.trim());
            sentimentService.analyzeReview(review);

            boolean saved = reviewDAO.createReview(review);
            if (saved) {
                response.sendRedirect("review?bookingId=" + bookingId + "&msg=submitted");
            } else {
                response.sendRedirect("review?bookingId=" + bookingId + "&error=save_failed");
            }

        } catch (NumberFormatException e) {
            response.sendRedirect("review?error=invalid_input");
        }
    }
}
