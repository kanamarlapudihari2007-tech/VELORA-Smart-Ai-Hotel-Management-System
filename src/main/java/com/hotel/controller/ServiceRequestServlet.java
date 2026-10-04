package com.hotel.controller;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.ServiceRequestDAO;
import com.hotel.model.Booking;
import com.hotel.model.ServiceRequest;
import com.hotel.model.ServiceRequestAIResult;
import com.hotel.model.User;
import com.hotel.service.ServiceRequestUnderstandingService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * ServiceRequestServlet - Stage 15 AI Service Request Understanding & Smart Item Extraction.
 * Handles submitting and tracking room service and housekeeping requests with automated Gemini AI extraction.
 * Mapped to /service-request
 */
@WebServlet("/service-request")
public class ServiceRequestServlet extends HttpServlet {

    private BookingDAO bookingDAO;
    private ServiceRequestDAO serviceRequestDAO;
    private ServiceRequestUnderstandingService understandingService;

    @Override
    public void init() throws ServletException {
        bookingDAO = new BookingDAO();
        serviceRequestDAO = new ServiceRequestDAO();
        understandingService = new ServiceRequestUnderstandingService();
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
        List<ServiceRequest> requests = serviceRequestDAO.getRequestsByGuestId(guestId);

        // Flash message for AI item extraction result
        Object aiResult = session.getAttribute("lastServiceAIResult");
        if (aiResult != null) {
            request.setAttribute("aiResult", aiResult);
            session.removeAttribute("lastServiceAIResult");
        }

        request.setAttribute("activeBooking", activeBooking);
        request.setAttribute("requests", requests);

        request.getRequestDispatcher("service-request.jsp").forward(request, response);
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
            request.setAttribute("errorMessage", "You must have an active reserved or checked-in stay to request room services.");
            doGet(request, response);
            return;
        }

        String requestType = request.getParameter("requestType");
        String rawText = request.getParameter("rawText");
        String extractedItems = request.getParameter("extractedItems");
        String priority = request.getParameter("priority");

        if (rawText == null || rawText.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Please describe your service request.");
            doGet(request, response);
            return;
        }

        // Stage 15: Google Gemini AI Request Understanding & Item Extraction
        ServiceRequestAIResult aiResult = understandingService.analyzeRequest(rawText, requestType, priority);

        // Allow manual item override if guest entered one explicitly
        String finalItems = (extractedItems != null && !extractedItems.trim().isEmpty())
                ? extractedItems.trim()
                : aiResult.getExtractedItems();

        ServiceRequest sr = new ServiceRequest();
        sr.setBookingId(activeBooking.getId());
        sr.setRoomId(activeBooking.getRoomId());
        sr.setRequestType(aiResult.getRequestType());
        sr.setRawText(rawText.trim());
        sr.setExtractedItems(finalItems);
        sr.setPriority(aiResult.getPriority());

        boolean success = serviceRequestDAO.createServiceRequest(sr);

        if (success) {
            session.setAttribute("lastServiceAIResult", aiResult);
            response.sendRedirect("service-request?msg=submitted");
        } else {
            request.setAttribute("errorMessage", "Failed to submit request due to a database error.");
            doGet(request, response);
        }
    }
}
