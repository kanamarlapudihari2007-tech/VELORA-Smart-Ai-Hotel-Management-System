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
import java.util.List;

/**
 * RoomManagementServlet - Handles room listing, creation, and status updates for Manager.
 * Mapped to /rooms
 */
@WebServlet("/rooms")
public class RoomManagementServlet extends HttpServlet {

    private RoomDAO roomDAO;

    @Override
    public void init() throws ServletException {
        roomDAO = new RoomDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Room> rooms = roomDAO.getAllRooms();
        List<RoomType> roomTypes = roomDAO.getAllRoomTypes();

        request.setAttribute("rooms", rooms);
        request.setAttribute("roomTypes", roomTypes);

        request.getRequestDispatcher("rooms-manage.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("add".equalsIgnoreCase(action)) {
            handleAddRoom(request, response);
        } else if ("updateStatus".equalsIgnoreCase(action)) {
            handleUpdateStatus(request, response);
        } else {
            response.sendRedirect("rooms");
        }
    }

    private void handleAddRoom(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String roomNumber = request.getParameter("roomNumber");
        String roomTypeIdStr = request.getParameter("roomTypeId");
        String floorNumberStr = request.getParameter("floorNumber");
        String status = request.getParameter("status");

        if (roomNumber == null || roomNumber.trim().isEmpty() ||
            roomTypeIdStr == null || floorNumberStr == null) {
            request.setAttribute("errorMessage", "All room parameters are required.");
            doGet(request, response);
            return;
        }

        roomNumber = roomNumber.trim();
        if (roomDAO.existsByRoomNumber(roomNumber)) {
            request.setAttribute("errorMessage", "Room number '" + roomNumber + "' already exists.");
            doGet(request, response);
            return;
        }

        try {
            int roomTypeId = Integer.parseInt(roomTypeIdStr);
            int floorNumber = Integer.parseInt(floorNumberStr);

            Room room = new Room(0, roomNumber, roomTypeId, floorNumber, status != null ? status : "AVAILABLE");
            boolean success = roomDAO.addRoom(room);

            if (success) {
                response.sendRedirect("rooms?msg=added");
            } else {
                request.setAttribute("errorMessage", "Failed to add room due to database error.");
                doGet(request, response);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Invalid numeric values for floor number or room type.");
            doGet(request, response);
        }
    }

    private void handleUpdateStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String roomIdStr = request.getParameter("roomId");
        String newStatus = request.getParameter("status");

        if (roomIdStr != null && newStatus != null) {
            try {
                int roomId = Integer.parseInt(roomIdStr);
                roomDAO.updateRoomStatus(roomId, newStatus);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect("rooms?msg=updated");
    }
}
