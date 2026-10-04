package com.hotel.controller;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.ReviewDAO;
import com.hotel.model.Booking;
import com.hotel.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * MyBookingsServlet - Displays all reservations made by the logged-in Guest.
 * Mapped to /my-bookings
 */
@WebServlet("/my-bookings")
public class MyBookingsServlet extends HttpServlet {

    private BookingDAO bookingDAO;
    private ReviewDAO reviewDAO;

    @Override
    public void init() throws ServletException {
        bookingDAO = new BookingDAO();
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
        boolean isManager = "MANAGER".equalsIgnoreCase(user.getRole());

        List<Booking> bookings;
        if (isManager) {
            bookings = bookingDAO.getAllBookings();
        } else {
            int guestId = bookingDAO.getGuestIdByUserId(user.getId());
            bookings = bookingDAO.getBookingsByGuestId(guestId);
            Set<Integer> reviewedBookingIds = reviewDAO.getReviewedBookingIdsForGuest(guestId);
            request.setAttribute("reviewedBookingIds", reviewedBookingIds);
        }

        request.setAttribute("bookings", bookings);
        request.getRequestDispatcher("my-bookings.jsp").forward(request, response);
    }
}
