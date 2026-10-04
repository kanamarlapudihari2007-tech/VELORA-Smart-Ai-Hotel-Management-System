package com.hotel.controller;

import com.hotel.dao.RoomDAO;
import com.hotel.model.Room;
import com.hotel.model.RoomType;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * RoomSearchServlet - Handles searching available rooms with date filtering.
 * Mapped to /search-rooms
 */
@WebServlet("/search-rooms")
public class RoomSearchServlet extends HttpServlet {

    private RoomDAO roomDAO;

    @Override
    public void init() throws ServletException {
        roomDAO = new RoomDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String checkInDate = request.getParameter("checkInDate");
        String checkOutDate = request.getParameter("checkOutDate");
        String roomTypeIdStr = request.getParameter("roomTypeId");

        Integer roomTypeId = null;
        if (roomTypeIdStr != null && !roomTypeIdStr.trim().isEmpty()) {
            try {
                roomTypeId = Integer.parseInt(roomTypeIdStr.trim());
            } catch (NumberFormatException e) {
                roomTypeId = null;
            }
        }

        // Default check-in/check-out dates if not provided
        if (checkInDate == null || checkInDate.trim().isEmpty()) {
            checkInDate = LocalDate.now().toString();
        }
        if (checkOutDate == null || checkOutDate.trim().isEmpty()) {
            checkOutDate = LocalDate.now().plusDays(1).toString();
        }

        long numberOfNights = 1;
        try {
            LocalDate inDate = LocalDate.parse(checkInDate);
            LocalDate outDate = LocalDate.parse(checkOutDate);
            if (outDate.isAfter(inDate)) {
                numberOfNights = ChronoUnit.DAYS.between(inDate, outDate);
            }
        } catch (Exception e) {
            numberOfNights = 1;
        }

        List<Room> availableRooms = roomDAO.searchAvailableRooms(checkInDate, checkOutDate, roomTypeId);
        List<RoomType> roomTypes = roomDAO.getAllRoomTypes();

        request.setAttribute("rooms", availableRooms);
        request.setAttribute("roomTypes", roomTypes);
        request.setAttribute("checkInDate", checkInDate);
        request.setAttribute("checkOutDate", checkOutDate);
        request.setAttribute("selectedRoomTypeId", roomTypeId);
        request.setAttribute("numberOfNights", numberOfNights);

        request.getRequestDispatcher("room-search.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
