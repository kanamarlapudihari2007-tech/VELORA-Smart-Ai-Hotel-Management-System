package com.hotel.dao;

import com.hotel.model.ServiceRequest;
import com.hotel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ServiceRequestDAO - Data Access Object for guest service & housekeeping requests.
 */
public class ServiceRequestDAO {

    /**
     * Creates a service request and automatically dispatches an associated task.
     */
    public boolean createServiceRequest(ServiceRequest sr) {
        String sqlSR = "INSERT INTO service_requests (booking_id, room_id, request_type, raw_text, extracted_items, priority, status) VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";
        String sqlTask = "INSERT INTO tasks (task_type, reference_id, priority, status, notes) VALUES ('SERVICE_REQUEST', ?, ?, 'ASSIGNED', ?)";

        Connection conn = null;
        PreparedStatement stmtSR = null;
        PreparedStatement stmtTask = null;
        ResultSet generatedKeys = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Transaction

            stmtSR = conn.prepareStatement(sqlSR, Statement.RETURN_GENERATED_KEYS);
            stmtSR.setInt(1, sr.getBookingId());
            stmtSR.setInt(2, sr.getRoomId());
            stmtSR.setString(3, sr.getRequestType());
            stmtSR.setString(4, sr.getRawText());
            stmtSR.setString(5, sr.getExtractedItems());
            stmtSR.setString(6, sr.getPriority() != null ? sr.getPriority() : "NORMAL");

            if (stmtSR.executeUpdate() == 0) {
                conn.rollback();
                return false;
            }

            generatedKeys = stmtSR.getGeneratedKeys();
            int srId = 0;
            if (generatedKeys.next()) {
                srId = generatedKeys.getInt(1);
            } else {
                conn.rollback();
                return false;
            }

            // Create Staff Task
            stmtTask = conn.prepareStatement(sqlTask, Statement.RETURN_GENERATED_KEYS);
            stmtTask.setInt(1, srId);
            stmtTask.setString(2, sr.getPriority() != null ? sr.getPriority() : "NORMAL");
            stmtTask.setString(3, "[" + sr.getRequestType() + "] " + (sr.getExtractedItems() != null ? sr.getExtractedItems() : sr.getRawText()));
            stmtTask.executeUpdate();

            int taskId = 0;
            try (ResultSet rsT = stmtTask.getGeneratedKeys()) {
                if (rsT.next()) {
                    taskId = rsT.getInt(1);
                }
            }

            conn.commit();

            // Trigger Intelligent Staff Task Assignment (Stage 12)
            if (taskId > 0) {
                String roomNumber = getRoomNumberById(sr.getRoomId());
                try {
                    new com.hotel.service.TaskAssignmentService().assignTaskAutomatically(
                        taskId, "SERVICE_REQUEST", sr.getRequestType(), sr.getPriority(), roomNumber
                    );
                } catch (Exception e) {
                    System.err.println("Warning: Automatic task assignment error: " + e.getMessage());
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error creating service request: " + e.getMessage());
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (stmtSR != null) try { stmtSR.close(); } catch (SQLException e) {}
            if (stmtTask != null) try { stmtTask.close(); } catch (SQLException e) {}
            DBConnection.closeConnection(conn);
        }
    }

    /**
     * Fetches all service requests submitted by a guest across their bookings.
     */
    public List<ServiceRequest> getRequestsByGuestId(int guestId) {
        List<ServiceRequest> list = new ArrayList<>();
        String sql = "SELECT sr.id, sr.booking_id, sr.room_id, sr.request_type, sr.raw_text, sr.extracted_items, " +
                     "sr.priority, sr.status, sr.created_at, r.room_number, b.booking_code " +
                     "FROM service_requests sr " +
                     "JOIN bookings b ON sr.booking_id = b.id " +
                     "JOIN rooms r ON sr.room_id = r.id " +
                     "WHERE b.guest_id = ? " +
                     "ORDER BY sr.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, guestId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ServiceRequest sr = new ServiceRequest();
                    sr.setId(rs.getInt("id"));
                    sr.setBookingId(rs.getInt("booking_id"));
                    sr.setRoomId(rs.getInt("room_id"));
                    sr.setRequestType(rs.getString("request_type"));
                    sr.setRawText(rs.getString("raw_text"));
                    sr.setExtractedItems(rs.getString("extracted_items"));
                    sr.setPriority(rs.getString("priority"));
                    sr.setStatus(rs.getString("status"));
                    sr.setCreatedAt(rs.getTimestamp("created_at"));
                    sr.setRoomNumber(rs.getString("room_number"));
                    sr.setBookingCode(rs.getString("booking_code"));
                    list.add(sr);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching service requests: " + e.getMessage());
        }
        return list;
    }

    /**
     * Fetches all service requests linked to a specific booking.
     */
    public List<ServiceRequest> getRequestsByBookingId(int bookingId) {
        List<ServiceRequest> list = new ArrayList<>();
        String sql = "SELECT sr.id, sr.booking_id, sr.room_id, sr.request_type, sr.raw_text, sr.extracted_items, " +
                     "sr.priority, sr.status, sr.created_at, r.room_number, b.booking_code " +
                     "FROM service_requests sr " +
                     "JOIN bookings b ON sr.booking_id = b.id " +
                     "JOIN rooms r ON sr.room_id = r.id " +
                     "WHERE sr.booking_id = ? " +
                     "ORDER BY sr.created_at ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ServiceRequest sr = new ServiceRequest();
                    sr.setId(rs.getInt("id"));
                    sr.setBookingId(rs.getInt("booking_id"));
                    sr.setRoomId(rs.getInt("room_id"));
                    sr.setRequestType(rs.getString("request_type"));
                    sr.setRawText(rs.getString("raw_text"));
                    sr.setExtractedItems(rs.getString("extracted_items"));
                    sr.setPriority(rs.getString("priority"));
                    sr.setStatus(rs.getString("status"));
                    sr.setCreatedAt(rs.getTimestamp("created_at"));
                    sr.setRoomNumber(rs.getString("room_number"));
                    sr.setBookingCode(rs.getString("booking_code"));
                    list.add(sr);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching service requests by booking_id: " + e.getMessage());
        }
        return list;
    }

    private String getRoomNumberById(int roomId) {
        String sql = "SELECT room_number FROM rooms WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("room_number");
            }
        } catch (SQLException ignored) {}
        return "N/A";
    }
}
