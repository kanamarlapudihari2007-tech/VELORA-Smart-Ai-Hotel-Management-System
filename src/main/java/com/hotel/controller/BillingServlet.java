package com.hotel.controller;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.PaymentDAO;
import com.hotel.dao.ServiceRequestDAO;
import com.hotel.model.Booking;
import com.hotel.model.Payment;
import com.hotel.model.ServiceRequest;
import com.hotel.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * BillingServlet - Comprehensive Guest & Manager Billing, Digital Invoice & Settlement Portal.
 * Handles /billing and /bill endpoints.
 */
@WebServlet(urlPatterns = {"/billing", "/bill"})
public class BillingServlet extends HttpServlet {

    private BookingDAO bookingDAO;
    private PaymentDAO paymentDAO;
    private ServiceRequestDAO serviceRequestDAO;

    @Override
    public void init() throws ServletException {
        bookingDAO = new BookingDAO();
        paymentDAO = new PaymentDAO();
        serviceRequestDAO = new ServiceRequestDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        boolean isManager = "MANAGER".equalsIgnoreCase(currentUser.getRole());
        int guestId = isManager ? 0 : bookingDAO.getGuestIdByUserId(currentUser.getId());

        List<Booking> availableBookings = isManager 
            ? bookingDAO.getAllBookings() 
            : bookingDAO.getBookingsByGuestId(guestId);

        String bookingIdParam = request.getParameter("bookingId");
        Booking selectedBooking = null;

        if (bookingIdParam != null && !bookingIdParam.trim().isEmpty()) {
            try {
                int requestedId = Integer.parseInt(bookingIdParam.trim());
                Booking b = bookingDAO.getBookingById(requestedId);
                if (b != null) {
                    // Security guard: Non-managers can only inspect their own bookings
                    if (isManager || b.getGuestId() == guestId) {
                        selectedBooking = b;
                    }
                }
            } catch (NumberFormatException ignored) {}
        }

        // If no specific booking requested, automatically pick active check-in or most recent
        if (selectedBooking == null && availableBookings != null && !availableBookings.isEmpty()) {
            for (Booking b : availableBookings) {
                if ("CHECKED_IN".equals(b.getStatus())) {
                    selectedBooking = bookingDAO.getBookingById(b.getId());
                    break;
                }
            }
            if (selectedBooking == null) {
                selectedBooking = bookingDAO.getBookingById(availableBookings.get(0).getId());
            }
        }

        // Attach billing breakdown if a booking is selected
        if (selectedBooking != null) {
            Payment payment = paymentDAO.getPaymentByBookingId(selectedBooking.getId());
            List<ServiceRequest> serviceRequests = serviceRequestDAO.getRequestsByBookingId(selectedBooking.getId());

            // Compute stay duration and rates
            long diffMs = selectedBooking.getCheckOutDate().getTime() - selectedBooking.getCheckInDate().getTime();
            long nights = Math.max(1, TimeUnit.MILLISECONDS.toDays(diffMs));

            BigDecimal nightlyRate = selectedBooking.getPricePerNight();
            if (nightlyRate == null || nightlyRate.compareTo(BigDecimal.ZERO) <= 0) {
                nightlyRate = selectedBooking.getTotalAmount().divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP);
            }

            BigDecimal baseTariff = nightlyRate.multiply(BigDecimal.valueOf(nights));
            // 12% Hotel GST standard
            BigDecimal taxAmount = baseTariff.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);

            request.setAttribute("booking", selectedBooking);
            request.setAttribute("payment", payment);
            request.setAttribute("serviceRequests", serviceRequests);
            request.setAttribute("nights", nights);
            request.setAttribute("nightlyRate", nightlyRate);
            request.setAttribute("baseTariff", baseTariff);
            request.setAttribute("taxAmount", taxAmount);
        }

        request.setAttribute("availableBookings", availableBookings);
        request.setAttribute("isManager", isManager);

        request.getRequestDispatcher("billing.jsp").forward(request, response);
    }
}
