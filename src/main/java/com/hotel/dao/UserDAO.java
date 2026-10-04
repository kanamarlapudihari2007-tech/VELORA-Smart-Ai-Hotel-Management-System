package com.hotel.dao;

import com.hotel.model.Guest;
import com.hotel.model.User;
import com.hotel.util.DBConnection;
import com.hotel.util.PasswordUtil;

import java.sql.*;

/**
 * UserDAO - Data Access Object for handling user registration, queries, and authentication.
 * Follows Rule #11: Uses DAO pattern and PreparedStatements to prevent SQL injection.
 */
public class UserDAO {

    /**
     * Registers a new Guest user with atomic transaction management.
     * Inserts records into both 'users' and 'guests' tables.
     * @param user User credentials entity
     * @param guest Guest profile entity
     * @return boolean true if successful, false otherwise
     */
    public boolean registerGuest(User user, Guest guest) {
        String sqlUser = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, 'GUEST')";
        String sqlGuest = "INSERT INTO guests (user_id, full_name, phone, id_proof, address) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmtUser = null;
        PreparedStatement stmtGuest = null;
        ResultSet generatedKeys = null;

        try {
            conn = DBConnection.getConnection();
            // Start Transaction
            conn.setAutoCommit(false);

            // 1. Insert User Account
            stmtUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            stmtUser.setString(1, user.getUsername());
            stmtUser.setString(2, user.getPassword()); // Should already be hashed
            stmtUser.setString(3, user.getEmail());

            int affectedRows = stmtUser.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }

            // Obtain auto-incremented user_id
            generatedKeys = stmtUser.getGeneratedKeys();
            int userId = 0;
            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            } else {
                conn.rollback();
                return false;
            }

            // 2. Insert Guest Profile
            stmtGuest = conn.prepareStatement(sqlGuest);
            stmtGuest.setInt(1, userId);
            stmtGuest.setString(2, guest.getFullName());
            stmtGuest.setString(3, guest.getPhone());
            stmtGuest.setString(4, guest.getIdProof());
            stmtGuest.setString(5, guest.getAddress());

            stmtGuest.executeUpdate();

            // Commit Transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error during registerGuest: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (stmtUser != null) try { stmtUser.close(); } catch (SQLException e) {}
            if (stmtGuest != null) try { stmtGuest.close(); } catch (SQLException e) {}
            DBConnection.closeConnection(conn);
        }
    }

    /**
     * Finds a User record by username.
     * @param username Target username
     * @return User object or null if not found
     */
    public User findByUsername(String username) {
        String sql = "SELECT id, username, password, email, role, created_at FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getTimestamp("created_at")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    /**
     * Checks if a username is already taken.
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking username existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Checks if an email is already registered.
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Authenticates user with username and raw password.
     * @return Authenticated User object or null if credentials are invalid.
     */
    public User authenticate(String username, String rawPassword) {
        User user = findByUsername(username);
        if (user != null) {
            if (PasswordUtil.checkPassword(rawPassword, user.getPassword())) {
                return user;
            }
        }
        return null;
    }
}
