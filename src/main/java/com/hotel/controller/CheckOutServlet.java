package com.hotel.controller;

import com.hotel.dao.BookingDAO;
import com.hotel.model.Booking;
import com.hotel.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * CheckOutServlet - Handles Guest Checkout & Billing Breakdown.
 * Mapped to /checkout
 */
@WebServlet("/checkout")
public class CheckOutServlet extends HttpServlet {

    private BookingDAO bookingDAO;

    @Override
    public void init() throws ServletException {
        bookingDAO = new BookingDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        String bookingIdStr = request.getParameter("bookingId");
        if (bookingIdStr != null && !bookingIdStr.trim().isEmpty()) {
            response.sendRedirect("billing?bookingId=" + bookingIdStr.trim());
        } else {
            response.sendRedirect("billing");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        String bookingIdStr = request.getParameter("bookingId");
        if (bookingIdStr != null && !bookingIdStr.trim().isEmpty()) {
            try {
                int bookingId = Integer.parseInt(bookingIdStr.trim());
                boolean success = bookingDAO.checkOutBooking(bookingId);
                if (success) {
                    response.sendRedirect("billing?bookingId=" + bookingId + "&msg=checked_out_success");
                    return;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect("billing?error=checkout_failed");
    }
}
