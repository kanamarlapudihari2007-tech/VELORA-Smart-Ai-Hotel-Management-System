package com.hotel.controller;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/**
 * BookRoomServlet - Handles room reservation confirmation and execution.
 * Mapped to /book-room
 */
@WebServlet("/book-room")
public class BookRoomServlet extends HttpServlet {

    private RoomDAO roomDAO;
    private BookingDAO bookingDAO;

    @Override
    public void init() throws ServletException {
        roomDAO = new RoomDAO();
        bookingDAO = new BookingDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String roomIdStr = request.getParameter("roomId");
        String checkInStr = request.getParameter("checkIn");
        String checkOutStr = request.getParameter("checkOut");

        if (roomIdStr == null || roomIdStr.trim().isEmpty()) {
            response.sendRedirect("search-rooms");
            return;
        }

        try {
            int roomId = Integer.parseInt(roomIdStr.trim());
            Room room = roomDAO.getRoomById(roomId);

            if (room == null) {
                response.sendRedirect("search-rooms");
                return;
            }

            LocalDate inDate = (checkInStr != null && !checkInStr.trim().isEmpty())
                    ? LocalDate.parse(checkInStr) : LocalDate.now();
            LocalDate outDate = (checkOutStr != null && !checkOutStr.trim().isEmpty())
                    ? LocalDate.parse(checkOutStr) : LocalDate.now().plusDays(1);

            long nights = ChronoUnit.DAYS.between(inDate, outDate);
            if (nights < 1) nights = 1;

            BigDecimal totalAmount = room.getPricePerNight().multiply(new BigDecimal(nights));

            request.setAttribute("room", room);
            request.setAttribute("checkInDate", inDate.toString());
            request.setAttribute("checkOutDate", outDate.toString());
            request.setAttribute("numberOfNights", nights);
            request.setAttribute("totalAmount", totalAmount);

            request.getRequestDispatcher("booking-confirm.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("search-rooms");
        }
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
        String roomIdStr = request.getParameter("roomId");
        String checkInStr = request.getParameter("checkInDate");
        String checkOutStr = request.getParameter("checkOutDate");
        String paymentMethod = request.getParameter("paymentMethod");

        if (roomIdStr == null || checkInStr == null || checkOutStr == null) {
            response.sendRedirect("search-rooms");
            return;
        }

        try {
            int roomId = Integer.parseInt(roomIdStr);
            Room room = roomDAO.getRoomById(roomId);

            int guestId = bookingDAO.getGuestIdByUserId(user.getId());
            if (guestId == 0) {
                request.setAttribute("errorMessage", "Guest profile not found. Please contact administration.");
                request.getRequestDispatcher("booking-confirm.jsp").forward(request, response);
                return;
            }

            LocalDate inDate = LocalDate.parse(checkInStr);
            LocalDate outDate = LocalDate.parse(checkOutStr);
            long nights = ChronoUnit.DAYS.between(inDate, outDate);
            if (nights < 1) nights = 1;

            BigDecimal totalAmount = room.getPricePerNight().multiply(new BigDecimal(nights));

            // Generate unique booking code
            String bookingCode = "BK-" + LocalDate.now().getYear() + "-" + (10000 + new Random().nextInt(90000));

            Booking booking = new Booking();
            booking.setBookingCode(bookingCode);
            booking.setGuestId(guestId);
            booking.setRoomId(roomId);
            booking.setCheckInDate(Date.valueOf(inDate));
            booking.setCheckOutDate(Date.valueOf(outDate));
            booking.setTotalAmount(totalAmount);

            boolean success = bookingDAO.createBooking(booking, paymentMethod);

            if (success) {
                response.sendRedirect("my-bookings?msg=booked&code=" + bookingCode);
            } else {
                request.setAttribute("errorMessage", "Reservation failed due to a database error. Please try again.");
                doGet(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("search-rooms");
        }
    }
}
