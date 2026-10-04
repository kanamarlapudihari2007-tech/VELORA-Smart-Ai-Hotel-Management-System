package com.hotel.dao;

import com.hotel.model.Complaint;
import com.hotel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ComplaintDAO - Data Access Object for guest issue complaints and staff task generation.
 */
public class ComplaintDAO {

    /**
     * Creates a complaint record and automatically dispatches a staff work task.
     */
    public boolean createComplaint(Complaint c) {
        String sqlComplaint = "INSERT INTO complaints (booking_id, room_id, raw_text, category, priority, short_description, status) VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";
        String sqlTask = "INSERT INTO tasks (task_type, reference_id, priority, status, notes) VALUES ('COMPLAINT', ?, ?, 'ASSIGNED', ?)";

        Connection conn = null;
        PreparedStatement stmtC = null;
        PreparedStatement stmtT = null;
        ResultSet generatedKeys = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            stmtC = conn.prepareStatement(sqlComplaint, Statement.RETURN_GENERATED_KEYS);
            stmtC.setInt(1, c.getBookingId());
            stmtC.setInt(2, c.getRoomId());
            stmtC.setString(3, c.getRawText());
            stmtC.setString(4, c.getCategory());
            stmtC.setString(5, c.getPriority() != null ? c.getPriority() : "NORMAL");
            stmtC.setString(6, c.getShortDescription());

            if (stmtC.executeUpdate() == 0) {
                conn.rollback();
                return false;
            }

            generatedKeys = stmtC.getGeneratedKeys();
            int complaintId = 0;
            if (generatedKeys.next()) {
                complaintId = generatedKeys.getInt(1);
            } else {
                conn.rollback();
                return false;
            }

            // Create Staff Task
            stmtT = conn.prepareStatement(sqlTask, Statement.RETURN_GENERATED_KEYS);
            stmtT.setInt(1, complaintId);
            stmtT.setString(2, c.getPriority() != null ? c.getPriority() : "NORMAL");
            stmtT.setString(3, "[" + c.getCategory() + "] " + c.getShortDescription());
            stmtT.executeUpdate();

            int taskId = 0;
            try (ResultSet rsT = stmtT.getGeneratedKeys()) {
                if (rsT.next()) {
                    taskId = rsT.getInt(1);
                }
            }

            // If HIGH or URGENT maintenance issue, flag room status to MAINTENANCE
            if ("HIGH".equalsIgnoreCase(c.getPriority()) || "URGENT".equalsIgnoreCase(c.getPriority())) {
                String sqlMaintRoom = "UPDATE rooms SET status = 'MAINTENANCE' WHERE id = ?";
                try (PreparedStatement stmtR = conn.prepareStatement(sqlMaintRoom)) {
                    stmtR.setInt(1, c.getRoomId());
                    stmtR.executeUpdate();
                }
            }

            conn.commit(); // Commit Transaction

            // Trigger Intelligent Staff Task Assignment (Stage 12)
            if (taskId > 0) {
                String roomNumber = getRoomNumberById(c.getRoomId());
                try {
                    new com.hotel.service.TaskAssignmentService().assignTaskAutomatically(
                        taskId, "COMPLAINT", c.getCategory(), c.getPriority(), roomNumber
                    );
                } catch (Exception e) {
                    System.err.println("Warning: Automatic task assignment error: " + e.getMessage());
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error creating complaint: " + e.getMessage());
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (stmtC != null) try { stmtC.close(); } catch (SQLException e) {}
            if (stmtT != null) try { stmtT.close(); } catch (SQLException e) {}
            DBConnection.closeConnection(conn);
        }
    }

    /**
     * Retrieves complaints submitted by a guest across their bookings.
     */
    public List<Complaint> getComplaintsByGuestId(int guestId) {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT c.id, c.booking_id, c.room_id, c.raw_text, c.category, c.priority, " +
                     "c.short_description, c.status, c.created_at, r.room_number, b.booking_code " +
                     "FROM complaints c " +
                     "JOIN bookings b ON c.booking_id = b.id " +
                     "JOIN rooms r ON c.room_id = r.id " +
                     "WHERE b.guest_id = ? " +
                     "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, guestId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Complaint c = new Complaint();
                    c.setId(rs.getInt("id"));
                    c.setBookingId(rs.getInt("booking_id"));
                    c.setRoomId(rs.getInt("room_id"));
                    c.setRawText(rs.getString("raw_text"));
                    c.setCategory(rs.getString("category"));
                    c.setPriority(rs.getString("priority"));
                    c.setShortDescription(rs.getString("short_description"));
                    c.setStatus(rs.getString("status"));
                    c.setCreatedAt(rs.getTimestamp("created_at"));
                    c.setRoomNumber(rs.getString("room_number"));
                    c.setBookingCode(rs.getString("booking_code"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching complaints by guest_id: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all complaints logged in the system (for Manager / Staff views).
     */
    public List<Complaint> getAllComplaints() {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT c.id, c.booking_id, c.room_id, c.raw_text, c.category, c.priority, " +
                     "c.short_description, c.status, c.created_at, r.room_number, b.booking_code, g.full_name as guest_name " +
                     "FROM complaints c " +
                     "JOIN bookings b ON c.booking_id = b.id " +
                     "JOIN rooms r ON c.room_id = r.id " +
                     "JOIN guests g ON b.guest_id = g.id " +
                     "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Complaint c = new Complaint();
                c.setId(rs.getInt("id"));
                c.setBookingId(rs.getInt("booking_id"));
                c.setRoomId(rs.getInt("room_id"));
                c.setRawText(rs.getString("raw_text"));
                c.setCategory(rs.getString("category"));
                c.setPriority(rs.getString("priority"));
                c.setShortDescription(rs.getString("short_description"));
                c.setStatus(rs.getString("status"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setRoomNumber(rs.getString("room_number"));
                c.setBookingCode(rs.getString("booking_code"));
                c.setGuestName(rs.getString("guest_name"));
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all complaints: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates complaint status (e.g., RESOLVED).
     */
    public boolean updateComplaintStatus(int complaintId, String status) {
        String sql = "UPDATE complaints SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, complaintId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating complaint status: " + e.getMessage());
        }
        return false;
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
