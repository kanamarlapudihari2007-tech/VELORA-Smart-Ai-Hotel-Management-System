package com.hotel.controller;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.ComplaintDAO;
import com.hotel.model.Booking;
import com.hotel.model.Complaint;
import com.hotel.model.ComplaintClassificationResult;
import com.hotel.model.User;
import com.hotel.service.ComplaintClassificationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * ComplaintServlet - Handles guest issue complaint logging and status tracking.
 * Enhanced in Stage 14 with Google Gemini AI Complaint Classification.
 * Mapped to /complaint
 */
@WebServlet("/complaint")
public class ComplaintServlet extends HttpServlet {

    private BookingDAO bookingDAO;
    private ComplaintDAO complaintDAO;
    private ComplaintClassificationService classificationService;

    @Override
    public void init() throws ServletException {
        bookingDAO = new BookingDAO();
        complaintDAO = new ComplaintDAO();
        classificationService = new ComplaintClassificationService();
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
        int guestId = bookingDAO.getGuestIdByUserId(user.getId());

        Booking activeBooking = bookingDAO.getActiveBookingForGuest(guestId);
        List<Complaint> complaints = complaintDAO.getComplaintsByGuestId(guestId);

        // Flash message for AI classification results
        Object aiClassification = session.getAttribute("lastAIClassification");
        if (aiClassification != null) {
            request.setAttribute("aiClassification", aiClassification);
            session.removeAttribute("lastAIClassification");
        }

        request.setAttribute("activeBooking", activeBooking);
        request.setAttribute("complaints", complaints);

        request.getRequestDispatcher("complaint.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        User user = (User) session.getAttribute("user");
        int guestId = bookingDAO.getGuestIdByUserId(user.getId());
        Booking activeBooking = bookingDAO.getActiveBookingForGuest(guestId);

        if (activeBooking == null) {
            request.setAttribute("errorMessage", "You must have an active reservation or stay to log a complaint.");
            doGet(request, response);
            return;
        }

        String rawText = request.getParameter("rawText");
        String category = request.getParameter("category");
        String priority = request.getParameter("priority");
        String shortDescription = request.getParameter("shortDescription");

        if (rawText == null || rawText.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Please describe the issue you are experiencing.");
            doGet(request, response);
            return;
        }

        // Stage 14: Google Gemini AI Intelligent Classification
        ComplaintClassificationResult aiResult = classificationService.classifyComplaint(rawText, category, priority);

        // Allow manual short description override if explicitly entered by the guest
        String finalShortDesc = (shortDescription != null && !shortDescription.trim().isEmpty())
                ? shortDescription.trim()
                : aiResult.getShortDescription();

        Complaint complaint = new Complaint();
        complaint.setBookingId(activeBooking.getId());
        complaint.setRoomId(activeBooking.getRoomId());
        complaint.setCategory(aiResult.getCategory());
        complaint.setPriority(aiResult.getPriority());
        complaint.setShortDescription(finalShortDesc);
        complaint.setRawText(rawText.trim());

        boolean success = complaintDAO.createComplaint(complaint);

        if (success) {
            session.setAttribute("lastAIClassification", aiResult);
            response.sendRedirect("complaint?msg=submitted");
        } else {
            request.setAttribute("errorMessage", "Failed to log complaint due to a database error.");
            doGet(request, response);
        }
    }
}
