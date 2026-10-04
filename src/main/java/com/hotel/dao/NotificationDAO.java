package com.hotel.dao;

import com.hotel.model.Notification;
import com.hotel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationDAO - Data Access Object for handling user alerts and staff task notifications.
 */
public class NotificationDAO {

    /**
     * Creates a new notification for a user.
     */
    public boolean createNotification(int userId, String title, String message) {
        String sql = "INSERT INTO notifications (user_id, title, message, is_read) VALUES (?, ?, ?, FALSE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, title);
            stmt.setString(3, message);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating notification: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sends a notification to all active Managers (e.g. when automatic task assignment requires manual review).
     */
    public void notifyAllManagers(String title, String message) {
        String sqlSelect = "SELECT id FROM users WHERE role = 'MANAGER'";
        String sqlInsert = "INSERT INTO notifications (user_id, title, message, is_read) VALUES (?, ?, ?, FALSE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmtSelect = conn.prepareStatement(sqlSelect);
             ResultSet rs = stmtSelect.executeQuery()) {

            List<Integer> managerIds = new ArrayList<>();
            while (rs.next()) {
                managerIds.add(rs.getInt("id"));
            }

            if (!managerIds.isEmpty()) {
                try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                    for (int mgrId : managerIds) {
                        stmtInsert.setInt(1, mgrId);
                        stmtInsert.setString(2, title);
                        stmtInsert.setString(3, message);
                        stmtInsert.addBatch();
                    }
                    stmtInsert.executeBatch();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error notifying managers: " + e.getMessage());
        }
    }

    /**
     * Fetches recent notifications for a user.
     */
    public List<Notification> getNotificationsByUserId(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT id, user_id, title, message, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 20";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("id"));
                    n.setUserId(rs.getInt("user_id"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notifications: " + e.getMessage());
        }
        return list;
    }

    /**
     * Gets the count of unread notifications for a user.
     */
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching unread count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Marks a specific notification as read.
     */
    public boolean markAsRead(int notificationId, int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notificationId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error marking notification read: " + e.getMessage());
            return false;
        }
    }
}
