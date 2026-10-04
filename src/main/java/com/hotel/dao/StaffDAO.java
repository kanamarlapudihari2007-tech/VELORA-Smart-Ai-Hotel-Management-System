package com.hotel.dao;

import com.hotel.model.Staff;
import com.hotel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StaffDAO - Data Access Object for managing hotel employee profiles, work statuses,
 * active workloads, and performance credits.
 */
public class StaffDAO {

    public StaffDAO() {
        ensurePerformanceCreditsColumn();
        ensureSectorStaffExists();
    }

    /**
     * Safely ensures the 'performance_credits' column exists in the 'staff' table without dropping or recreating tables.
     */
    private void ensurePerformanceCreditsColumn() {
        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getColumns(null, null, "staff", "performance_credits")) {
                if (!rs.next()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("ALTER TABLE staff ADD COLUMN performance_credits INT NOT NULL DEFAULT 100");
                    }
                }
            }
        } catch (SQLException e) {
            // Column may already exist or table not initialized yet, safe to proceed
        }
    }

    /**
     * Automatically ensures at least one active staff member exists for each of the 5 hotel operational sectors:
     * 1. Housekeeping (john_housekeeping)
     * 2. Maintenance (mike_maintenance)
     * 3. Food Service (david_foodservice)
     * 4. Reception (sarah_reception)
     * 5. Security (robert_security)
     */
    private void ensureSectorStaffExists() {
        String[][] sectors = {
            {"john_housekeeping", "staff123", "john@smarthotel.com", "John Doe", "9876543210", "Housekeeping"},
            {"mike_maintenance", "staff123", "mike@smarthotel.com", "Mike Smith", "9876543211", "Maintenance"},
            {"david_foodservice", "staff123", "david@smarthotel.com", "David Miller", "9876543212", "Food Service"},
            {"sarah_reception", "staff123", "sarah@smarthotel.com", "Sarah Jenkins", "9876543213", "Reception"},
            {"robert_security", "staff123", "robert@smarthotel.com", "Robert Taylor", "9876543214", "Security"}
        };

        for (String[] s : sectors) {
            ensureSingleStaff(s[0], s[1], s[2], s[3], s[4], s[5]);
        }
    }

    private void ensureSingleStaff(String username, String rawPassword, String email, String fullName, String phone, String category) {
        String sqlCheckUser = "SELECT id FROM users WHERE username = ?";
        String sqlInsertUser = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, 'STAFF')";
        String sqlCheckStaff = "SELECT id FROM staff WHERE user_id = ?";
        String sqlInsertStaff = "INSERT INTO staff (user_id, full_name, phone, category, work_status, performance_credits) VALUES (?, ?, ?, ?, 'AVAILABLE', 100)";

        try (Connection conn = DBConnection.getConnection()) {
            int userId = 0;

            // 1. Check or insert user account
            try (PreparedStatement stmt = conn.prepareStatement(sqlCheckUser)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    }
                }
            }

            if (userId == 0) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlInsertUser, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, username);
                    stmt.setString(2, rawPassword);
                    stmt.setString(3, email);
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            userId = rs.getInt(1);
                        }
                    }
                }
            }

            // 2. Check or insert staff profile
            if (userId > 0) {
                boolean staffExists = false;
                try (PreparedStatement stmt = conn.prepareStatement(sqlCheckStaff)) {
                    stmt.setInt(1, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            staffExists = true;
                        }
                    }
                }

                if (!staffExists) {
                    try (PreparedStatement stmt = conn.prepareStatement(sqlInsertStaff)) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, fullName);
                        stmt.setString(3, phone);
                        stmt.setString(4, category);
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            // Silently ignore if already seeded or database unavailable
        }
    }

    /**
     * Retrieves the Staff profile linked to a specific user account.
     */
    public Staff getStaffByUserId(int userId) {
        String sql = "SELECT s.id, s.user_id, s.full_name, s.phone, s.category, s.work_status, s.performance_credits, s.created_at, " +
                     "u.username, u.email, " +
                     "(SELECT COUNT(*) FROM tasks t WHERE t.assigned_staff_id = s.id AND t.status IN ('ASSIGNED', 'IN_PROGRESS')) AS active_workload " +
                     "FROM staff s " +
                     "JOIN users u ON s.user_id = u.id " +
                     "WHERE s.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStaff(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching staff by user_id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a Staff profile by staff table ID.
     */
    public Staff getStaffById(int id) {
        String sql = "SELECT s.id, s.user_id, s.full_name, s.phone, s.category, s.work_status, s.performance_credits, s.created_at, " +
                     "u.username, u.email, " +
                     "(SELECT COUNT(*) FROM tasks t WHERE t.assigned_staff_id = s.id AND t.status IN ('ASSIGNED', 'IN_PROGRESS')) AS active_workload " +
                     "FROM staff s " +
                     "JOIN users u ON s.user_id = u.id " +
                     "WHERE s.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStaff(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching staff by id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all staff members in the hotel, including their active task count.
     */
    public List<Staff> getAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        String sql = "SELECT s.id, s.user_id, s.full_name, s.phone, s.category, s.work_status, s.performance_credits, s.created_at, " +
                     "u.username, u.email, " +
                     "(SELECT COUNT(*) FROM tasks t WHERE t.assigned_staff_id = s.id AND t.status IN ('ASSIGNED', 'IN_PROGRESS')) AS active_workload " +
                     "FROM staff s " +
                     "JOIN users u ON s.user_id = u.id " +
                     "ORDER BY s.category ASC, s.full_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                staffList.add(mapResultSetToStaff(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all staff: " + e.getMessage());
        }
        return staffList;
    }

    /**
     * Retrieves all staff members belonging to an operational category along with their real-time active workload.
     * Used directly by the task assignment algorithm.
     */
    public List<Staff> getStaffWithWorkloadByCategory(String category) {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT s.id, s.user_id, s.full_name, s.phone, s.category, s.work_status, s.performance_credits, s.created_at, " +
                     "u.username, u.email, " +
                     "(SELECT COUNT(*) FROM tasks t WHERE t.assigned_staff_id = s.id AND t.status IN ('ASSIGNED', 'IN_PROGRESS')) AS active_workload " +
                     "FROM staff s " +
                     "JOIN users u ON s.user_id = u.id " +
                     "WHERE s.category = ? " +
                     "ORDER BY active_workload ASC, s.performance_credits DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStaff(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching staff by category: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates the active work status of a staff member (AVAILABLE, BUSY, OFF_DUTY).
     */
    public boolean updateWorkStatus(int staffId, String workStatus) {
        String sql = "UPDATE staff SET work_status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, workStatus);
            stmt.setInt(2, staffId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating staff work status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Safely adjusts a staff member's performance credits (e.g. +10 for completion, +20 for urgent).
     * Prevents negative credits with GREATEST(0, ...).
     */
    public boolean adjustPerformanceCredits(int staffId, int pointsDelta) {
        String sql = "UPDATE staff SET performance_credits = GREATEST(0, performance_credits + ?) WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pointsDelta);
            stmt.setInt(2, staffId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adjusting performance credits: " + e.getMessage());
            return false;
        }
    }

    private Staff mapResultSetToStaff(ResultSet rs) throws SQLException {
        Staff staff = new Staff();
        staff.setId(rs.getInt("id"));
        staff.setUserId(rs.getInt("user_id"));
        staff.setFullName(rs.getString("full_name"));
        staff.setPhone(rs.getString("phone"));
        staff.setCategory(rs.getString("category"));
        staff.setWorkStatus(rs.getString("work_status"));

        try {
            staff.setPerformanceCredits(rs.getInt("performance_credits"));
        } catch (SQLException e) {
            staff.setPerformanceCredits(100);
        }

        staff.setCreatedAt(rs.getTimestamp("created_at"));
        staff.setUsername(rs.getString("username"));
        staff.setEmail(rs.getString("email"));

        try {
            staff.setActiveTaskCount(rs.getInt("active_workload"));
        } catch (SQLException e) {
            staff.setActiveTaskCount(0);
        }

        return staff;
    }
}
