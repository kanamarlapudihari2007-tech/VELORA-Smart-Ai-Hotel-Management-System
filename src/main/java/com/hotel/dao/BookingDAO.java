package com.hotel.dao;

import com.hotel.model.Booking;
import com.hotel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingDAO - Data Access Object for handling reservation transactions, queries, and status.
 */
public class BookingDAO {

    /**
     * Executes an atomic multi-table transaction to create a booking, insert a payment record,
     * and update room status to 'RESERVED'.
     */
    public boolean createBooking(Booking booking, String paymentMethod) {
        String sqlBooking = "INSERT INTO bookings (booking_code, guest_id, room_id, check_in_date, check_out_date, status, total_amount) VALUES (?, ?, ?, ?, ?, 'RESERVED', ?)";
        String sqlPayment = "INSERT INTO payments (booking_id, amount, payment_method, payment_status) VALUES (?, ?, ?, 'COMPLETED')";
        String sqlUpdateRoom = "UPDATE rooms SET status = 'RESERVED' WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmtBooking = null;
        PreparedStatement stmtPayment = null;
        PreparedStatement stmtRoom = null;
        ResultSet generatedKeys = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Insert Booking Record
            stmtBooking = conn.prepareStatement(sqlBooking, Statement.RETURN_GENERATED_KEYS);
            stmtBooking.setString(1, booking.getBookingCode());
            stmtBooking.setInt(2, booking.getGuestId());
            stmtBooking.setInt(3, booking.getRoomId());
            stmtBooking.setDate(4, booking.getCheckInDate());
            stmtBooking.setDate(5, booking.getCheckOutDate());
            stmtBooking.setBigDecimal(6, booking.getTotalAmount());

            int affected = stmtBooking.executeUpdate();
            if (affected == 0) {
                conn.rollback();
                return false;
            }

            generatedKeys = stmtBooking.getGeneratedKeys();
            int bookingId = 0;
            if (generatedKeys.next()) {
                bookingId = generatedKeys.getInt(1);
            } else {
                conn.rollback();
                return false;
            }

            // 2. Insert Payment Record
            stmtPayment = conn.prepareStatement(sqlPayment);
            stmtPayment.setInt(1, bookingId);
            stmtPayment.setBigDecimal(2, booking.getTotalAmount());
            stmtPayment.setString(3, paymentMethod != null ? paymentMethod : "CARD");
            stmtPayment.executeUpdate();

            // 3. Update Room Status to RESERVED
            stmtRoom = conn.prepareStatement(sqlUpdateRoom);
            stmtRoom.setInt(1, booking.getRoomId());
            stmtRoom.executeUpdate();

            // Commit Transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error creating booking transaction: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (stmtBooking != null) try { stmtBooking.close(); } catch (SQLException e) {}
            if (stmtPayment != null) try { stmtPayment.close(); } catch (SQLException e) {}
            if (stmtRoom != null) try { stmtRoom.close(); } catch (SQLException e) {}
            DBConnection.closeConnection(conn);
        }
    }

    /**
     * Looks up guest.id using user_id.
     */
    public int getGuestIdByUserId(int userId) {
        String sql = "SELECT id FROM guests WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting guest_id by user_id: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves all bookings made by a specific guest.
     */
    public List<Booking> getBookingsByGuestId(int guestId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.id, b.booking_code, b.guest_id, b.room_id, b.check_in_date, b.check_out_date, " +
                     "b.status, b.total_amount, b.created_at, r.room_number, rt.type_name, rt.price_per_night " +
                     "FROM bookings b " +
                     "JOIN rooms r ON b.room_id = r.id " +
                     "JOIN room_types rt ON r.room_type_id = rt.id " +
                     "WHERE b.guest_id = ? " +
                     "ORDER BY b.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, guestId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking();
                    b.setId(rs.getInt("id"));
                    b.setBookingCode(rs.getString("booking_code"));
                    b.setGuestId(rs.getInt("guest_id"));
                    b.setRoomId(rs.getInt("room_id"));
                    b.setCheckInDate(rs.getDate("check_in_date"));
                    b.setCheckOutDate(rs.getDate("check_out_date"));
                    b.setStatus(rs.getString("status"));
                    b.setTotalAmount(rs.getBigDecimal("total_amount"));
                    b.setCreatedAt(rs.getTimestamp("created_at"));
                    b.setRoomNumber(rs.getString("room_number"));
                    b.setTypeName(rs.getString("type_name"));
                    b.setPricePerNight(rs.getBigDecimal("price_per_night"));
                    bookings.add(b);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bookings by guest_id: " + e.getMessage());
        }
        return bookings;
    }

    /**
     * Retrieves all bookings across the hotel (for Manager portal).
     */
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.id, b.booking_code, b.guest_id, b.room_id, b.check_in_date, b.check_out_date, " +
                     "b.status, b.total_amount, b.created_at, r.room_number, rt.type_name, rt.price_per_night, " +
                     "g.full_name as guest_name, g.phone as guest_phone " +
                     "FROM bookings b " +
                     "JOIN rooms r ON b.room_id = r.id " +
                     "JOIN room_types rt ON r.room_type_id = rt.id " +
                     "JOIN guests g ON b.guest_id = g.id " +
                     "ORDER BY b.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Booking b = new Booking();
                b.setId(rs.getInt("id"));
                b.setBookingCode(rs.getString("booking_code"));
                b.setGuestId(rs.getInt("guest_id"));
                b.setRoomId(rs.getInt("room_id"));
                b.setCheckInDate(rs.getDate("check_in_date"));
                b.setCheckOutDate(rs.getDate("check_out_date"));
                b.setStatus(rs.getString("status"));
                b.setTotalAmount(rs.getBigDecimal("total_amount"));
                b.setCreatedAt(rs.getTimestamp("created_at"));
                b.setRoomNumber(rs.getString("room_number"));
                b.setTypeName(rs.getString("type_name"));
                b.setPricePerNight(rs.getBigDecimal("price_per_night"));
                b.setGuestName(rs.getString("guest_name"));
                b.setGuestPhone(rs.getString("guest_phone"));
                bookings.add(b);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all bookings: " + e.getMessage());
        }
        return bookings;
    }

    /**
     * Retrieves active booking for a guest and room (if any).
     */
    public Booking getActiveBookingForGuest(int guestId) {
        String sql = "SELECT b.id, b.booking_code, b.guest_id, b.room_id, b.check_in_date, b.check_out_date, " +
                     "b.status, b.total_amount, b.created_at, r.room_number, rt.type_name, rt.price_per_night " +
                     "FROM bookings b " +
                     "JOIN rooms r ON b.room_id = r.id " +
                     "JOIN room_types rt ON r.room_type_id = rt.id " +
                     "WHERE b.guest_id = ? AND b.status IN ('RESERVED', 'CHECKED_IN') " +
                     "ORDER BY b.id DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, guestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Booking b = new Booking();
                    b.setId(rs.getInt("id"));
                    b.setBookingCode(rs.getString("booking_code"));
                    b.setGuestId(rs.getInt("guest_id"));
                    b.setRoomId(rs.getInt("room_id"));
                    b.setCheckInDate(rs.getDate("check_in_date"));
                    b.setCheckOutDate(rs.getDate("check_out_date"));
                    b.setStatus(rs.getString("status"));
                    b.setTotalAmount(rs.getBigDecimal("total_amount"));
                    b.setCreatedAt(rs.getTimestamp("created_at"));
                    b.setRoomNumber(rs.getString("room_number"));
                    b.setTypeName(rs.getString("type_name"));
                    b.setPricePerNight(rs.getBigDecimal("price_per_night"));
                    return b;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active booking: " + e.getMessage());
        }
        return null;
    }

    /**
     * Fetches booking by primary key ID.
     */
    public Booking getBookingById(int bookingId) {
        String sql = "SELECT b.id, b.booking_code, b.guest_id, b.room_id, b.check_in_date, b.check_out_date, " +
                     "b.status, b.total_amount, b.created_at, r.room_number, rt.type_name, rt.price_per_night, " +
                     "g.full_name as guest_name, g.phone as guest_phone " +
                     "FROM bookings b " +
                     "JOIN rooms r ON b.room_id = r.id " +
                     "JOIN room_types rt ON r.room_type_id = rt.id " +
                     "JOIN guests g ON b.guest_id = g.id " +
                     "WHERE b.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Booking b = new Booking();
                    b.setId(rs.getInt("id"));
                    b.setBookingCode(rs.getString("booking_code"));
                    b.setGuestId(rs.getInt("guest_id"));
                    b.setRoomId(rs.getInt("room_id"));
                    b.setCheckInDate(rs.getDate("check_in_date"));
                    b.setCheckOutDate(rs.getDate("check_out_date"));
                    b.setStatus(rs.getString("status"));
                    b.setTotalAmount(rs.getBigDecimal("total_amount"));
                    b.setCreatedAt(rs.getTimestamp("created_at"));
                    b.setRoomNumber(rs.getString("room_number"));
                    b.setTypeName(rs.getString("type_name"));
                    b.setPricePerNight(rs.getBigDecimal("price_per_night"));
                    b.setGuestName(rs.getString("guest_name"));
                    b.setGuestPhone(rs.getString("guest_phone"));
                    return b;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching booking by id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Executes check-in: Updates booking status to CHECKED_IN and room status to OCCUPIED.
     */
    public boolean checkInBooking(int bookingId) {
        Booking booking = getBookingById(bookingId);
        if (booking == null || !"RESERVED".equals(booking.getStatus())) {
            return false;
        }

        String sqlBooking = "UPDATE bookings SET status = 'CHECKED_IN' WHERE id = ?";
        String sqlRoom = "UPDATE rooms SET status = 'OCCUPIED' WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmtB = conn.prepareStatement(sqlBooking);
                 PreparedStatement stmtR = conn.prepareStatement(sqlRoom)) {
                stmtB.setInt(1, bookingId);
                stmtB.executeUpdate();

                stmtR.setInt(1, booking.getRoomId());
                stmtR.executeUpdate();

                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    /**
     * Executes check-out: Updates booking to CHECKED_OUT, room to CLEANING, and creates cleaning task.
     */
    public boolean checkOutBooking(int bookingId) {
        Booking booking = getBookingById(bookingId);
        if (booking == null || !"CHECKED_IN".equals(booking.getStatus())) {
            return false;
        }

        String sqlBooking = "UPDATE bookings SET status = 'CHECKED_OUT' WHERE id = ?";
        String sqlRoom = "UPDATE rooms SET status = 'CLEANING' WHERE id = ?";
        String sqlTask = "INSERT INTO tasks (task_type, reference_id, priority, status, notes) VALUES ('ROOM_CLEANING', ?, 'NORMAL', 'ASSIGNED', ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmtB = conn.prepareStatement(sqlBooking);
                 PreparedStatement stmtR = conn.prepareStatement(sqlRoom);
                 PreparedStatement stmtT = conn.prepareStatement(sqlTask, Statement.RETURN_GENERATED_KEYS)) {

                stmtB.setInt(1, bookingId);
                stmtB.executeUpdate();

                stmtR.setInt(1, booking.getRoomId());
                stmtR.executeUpdate();

                stmtT.setInt(1, booking.getRoomId());
                stmtT.setString(2, "Post-checkout cleaning required for Room " + booking.getRoomNumber());
                stmtT.executeUpdate();

                int taskId = 0;
                try (ResultSet rsT = stmtT.getGeneratedKeys()) {
                    if (rsT.next()) {
                        taskId = rsT.getInt(1);
                    }
                }

                conn.commit();

                // Trigger Intelligent Staff Task Assignment for Housekeeping (Stage 12)
                if (taskId > 0) {
                    try {
                        new com.hotel.service.TaskAssignmentService().assignTaskAutomatically(
                            taskId, "ROOM_CLEANING", "Housekeeping", "NORMAL", booking.getRoomNumber()
                        );
                    } catch (Exception e) {
                        System.err.println("Warning: Automatic cleaning task assignment error: " + e.getMessage());
                    }
                }

                return true;
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
}
