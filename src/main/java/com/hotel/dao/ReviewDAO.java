package com.hotel.dao;

import com.hotel.model.Review;
import com.hotel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ReviewDAO - Data Access Object for Guest Reviews & AI Sentiment Metrics.
 * Supports auto-table creation, review persistence, and executive reputation analytics.
 */
public class ReviewDAO {

    public ReviewDAO() {
        ensureTableExists();
    }

    /**
     * Automatically initializes the 'reviews' table if not already created.
     */
    public void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS reviews ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "booking_id INT NOT NULL,"
                + "guest_id INT NOT NULL,"
                + "rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),"
                + "review_text TEXT NOT NULL,"
                + "sentiment VARCHAR(20) DEFAULT 'NEUTRAL',"
                + "sentiment_score DECIMAL(4, 2) DEFAULT 0.00,"
                + "aspect_cleanliness VARCHAR(20) DEFAULT 'N/A',"
                + "aspect_staff VARCHAR(20) DEFAULT 'N/A',"
                + "aspect_room VARCHAR(20) DEFAULT 'N/A',"
                + "aspect_food VARCHAR(20) DEFAULT 'N/A',"
                + "aspect_value VARCHAR(20) DEFAULT 'N/A',"
                + "key_highlights TEXT,"
                + "ai_reply_draft TEXT,"
                + "manager_reply TEXT,"
                + "is_escalated BOOLEAN DEFAULT FALSE,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "CONSTRAINT fk_review_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,"
                + "CONSTRAINT fk_review_guest FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE CASCADE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Warning: Review table check/creation error: " + e.getMessage());
        }
    }

    /**
     * Persists a new guest review with its AI sentiment analysis.
     */
    public boolean createReview(Review r) {
        String sql = "INSERT INTO reviews (booking_id, guest_id, rating, review_text, sentiment, "
                + "sentiment_score, aspect_cleanliness, aspect_staff, aspect_room, aspect_food, "
                + "aspect_value, key_highlights, ai_reply_draft, is_escalated) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, r.getBookingId());
            stmt.setInt(2, r.getGuestId());
            stmt.setInt(3, r.getRating());
            stmt.setString(4, r.getReviewText());
            stmt.setString(5, r.getSentiment() != null ? r.getSentiment() : "NEUTRAL");
            stmt.setDouble(6, r.getSentimentScore());
            stmt.setString(7, r.getAspectCleanliness() != null ? r.getAspectCleanliness() : "N/A");
            stmt.setString(8, r.getAspectStaff() != null ? r.getAspectStaff() : "N/A");
            stmt.setString(9, r.getAspectRoom() != null ? r.getAspectRoom() : "N/A");
            stmt.setString(10, r.getAspectFood() != null ? r.getAspectFood() : "N/A");
            stmt.setString(11, r.getAspectValue() != null ? r.getAspectValue() : "N/A");
            stmt.setString(12, r.getKeyHighlights());
            stmt.setString(13, r.getAiReplyDraft());
            stmt.setBoolean(14, r.isEscalated());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        r.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving review: " + e.getMessage());
        }
        return false;
    }

    /**
     * Checks if a booking has already received a review.
     */
    public Review getReviewByBookingId(int bookingId) {
        String sql = "SELECT r.*, g.full_name as guest_name, b.booking_code, rm.room_number, rt.type_name "
                + "FROM reviews r "
                + "JOIN guests g ON r.guest_id = g.id "
                + "JOIN bookings b ON r.booking_id = b.id "
                + "JOIN rooms rm ON b.room_id = rm.id "
                + "JOIN room_types rt ON rm.room_type_id = rt.id "
                + "WHERE r.booking_id = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToReview(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching review by booking_id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Fetches all reviews across the hotel for Manager Dashboard.
     */
    public List<Review> getAllReviews() {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, g.full_name as guest_name, b.booking_code, rm.room_number, rt.type_name "
                + "FROM reviews r "
                + "JOIN guests g ON r.guest_id = g.id "
                + "JOIN bookings b ON r.booking_id = b.id "
                + "JOIN rooms rm ON b.room_id = rm.id "
                + "JOIN room_types rt ON rm.room_type_id = rt.id "
                + "ORDER BY r.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToReview(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all reviews: " + e.getMessage());
        }
        return list;
    }

    /**
     * Fetches all reviews written by a specific guest.
     */
    public List<Review> getReviewsByGuestId(int guestId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, g.full_name as guest_name, b.booking_code, rm.room_number, rt.type_name "
                + "FROM reviews r "
                + "JOIN guests g ON r.guest_id = g.id "
                + "JOIN bookings b ON r.booking_id = b.id "
                + "JOIN rooms rm ON b.room_id = rm.id "
                + "JOIN room_types rt ON rm.room_type_id = rt.id "
                + "WHERE r.guest_id = ? "
                + "ORDER BY r.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, guestId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToReview(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching guest reviews: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates official manager response to a review.
     */
    public boolean updateManagerReply(int reviewId, String reply) {
        String sql = "UPDATE reviews SET manager_reply = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reply);
            stmt.setInt(2, reviewId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating manager reply: " + e.getMessage());
            return false;
        }
    }

    /**
     * Computes high-level reputation and sentiment metrics for executive dashboards.
     */
    public Map<String, Object> getSentimentMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        String sql = "SELECT COUNT(*) as total, "
                + "COALESCE(AVG(rating), 0) as avg_rating, "
                + "SUM(CASE WHEN sentiment = 'POSITIVE' THEN 1 ELSE 0 END) as positive_cnt, "
                + "SUM(CASE WHEN sentiment = 'NEUTRAL' THEN 1 ELSE 0 END) as neutral_cnt, "
                + "SUM(CASE WHEN sentiment = 'NEGATIVE' THEN 1 ELSE 0 END) as negative_cnt, "
                + "SUM(CASE WHEN sentiment = 'CRITICAL' OR is_escalated = 1 THEN 1 ELSE 0 END) as escalated_cnt, "
                + "SUM(CASE WHEN aspect_cleanliness = 'POSITIVE' THEN 1 ELSE 0 END) as clean_pos, "
                + "SUM(CASE WHEN aspect_cleanliness IN ('POSITIVE', 'NEGATIVE') THEN 1 ELSE 0 END) as clean_total, "
                + "SUM(CASE WHEN aspect_staff = 'POSITIVE' THEN 1 ELSE 0 END) as staff_pos, "
                + "SUM(CASE WHEN aspect_staff IN ('POSITIVE', 'NEGATIVE') THEN 1 ELSE 0 END) as staff_total, "
                + "SUM(CASE WHEN aspect_room = 'POSITIVE' THEN 1 ELSE 0 END) as room_pos, "
                + "SUM(CASE WHEN aspect_room IN ('POSITIVE', 'NEGATIVE') THEN 1 ELSE 0 END) as room_total, "
                + "SUM(CASE WHEN aspect_food = 'POSITIVE' THEN 1 ELSE 0 END) as food_pos, "
                + "SUM(CASE WHEN aspect_food IN ('POSITIVE', 'NEGATIVE') THEN 1 ELSE 0 END) as food_total, "
                + "SUM(CASE WHEN aspect_value = 'POSITIVE' THEN 1 ELSE 0 END) as value_pos, "
                + "SUM(CASE WHEN aspect_value IN ('POSITIVE', 'NEGATIVE') THEN 1 ELSE 0 END) as value_total "
                + "FROM reviews";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int total = rs.getInt("total");
                metrics.put("totalReviews", total);
                metrics.put("avgRating", Math.round(rs.getDouble("avg_rating") * 10.0) / 10.0);
                metrics.put("positiveCount", rs.getInt("positive_cnt"));
                metrics.put("neutralCount", rs.getInt("neutral_cnt"));
                metrics.put("negativeCount", rs.getInt("negative_cnt"));
                metrics.put("escalatedCount", rs.getInt("escalated_cnt"));

                // Aspect percentages
                metrics.put("cleanlinessScore", calcPercent(rs.getInt("clean_pos"), rs.getInt("clean_total")));
                metrics.put("staffScore", calcPercent(rs.getInt("staff_pos"), rs.getInt("staff_total")));
                metrics.put("roomScore", calcPercent(rs.getInt("room_pos"), rs.getInt("room_total")));
                metrics.put("foodScore", calcPercent(rs.getInt("food_pos"), rs.getInt("food_total")));
                metrics.put("valueScore", calcPercent(rs.getInt("value_pos"), rs.getInt("value_total")));
            }
        } catch (SQLException e) {
            System.err.println("Error computing sentiment metrics: " + e.getMessage());
        }
        return metrics;
    }

    /**
     * Updates an existing review with freshly re-evaluated AI sentiment metrics.
     */
    public boolean updateReview(Review r) {
        String sql = "UPDATE reviews SET rating = ?, review_text = ?, sentiment = ?, "
                + "sentiment_score = ?, aspect_cleanliness = ?, aspect_staff = ?, "
                + "aspect_room = ?, aspect_food = ?, aspect_value = ?, key_highlights = ?, "
                + "ai_reply_draft = ?, is_escalated = ?, created_at = NOW() "
                + "WHERE id = ? AND guest_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, r.getRating());
            stmt.setString(2, r.getReviewText());
            stmt.setString(3, r.getSentiment() != null ? r.getSentiment() : "NEUTRAL");
            stmt.setDouble(4, r.getSentimentScore());
            stmt.setString(5, r.getAspectCleanliness() != null ? r.getAspectCleanliness() : "N/A");
            stmt.setString(6, r.getAspectStaff() != null ? r.getAspectStaff() : "N/A");
            stmt.setString(7, r.getAspectRoom() != null ? r.getAspectRoom() : "N/A");
            stmt.setString(8, r.getAspectFood() != null ? r.getAspectFood() : "N/A");
            stmt.setString(9, r.getAspectValue() != null ? r.getAspectValue() : "N/A");
            stmt.setString(10, r.getKeyHighlights());
            stmt.setString(11, r.getAiReplyDraft());
            stmt.setBoolean(12, r.isEscalated());
            stmt.setInt(13, r.getId());
            stmt.setInt(14, r.getGuestId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating review: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns a set of booking IDs for which the given guest has already submitted a review.
     */
    public Set<Integer> getReviewedBookingIdsForGuest(int guestId) {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT booking_id FROM reviews WHERE guest_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, guestId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("booking_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reviewed booking ids: " + e.getMessage());
        }
        return ids;
    }

    private int calcPercent(int positive, int total) {
        if (total == 0) return 90; // Default healthy baseline
        return (int) Math.round(((double) positive / total) * 100.0);
    }

    private Review mapRowToReview(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setId(rs.getInt("id"));
        r.setBookingId(rs.getInt("booking_id"));
        r.setGuestId(rs.getInt("guest_id"));
        r.setRating(rs.getInt("rating"));
        r.setReviewText(rs.getString("review_text"));
        r.setSentiment(rs.getString("sentiment"));
        r.setSentimentScore(rs.getDouble("sentiment_score"));
        r.setAspectCleanliness(rs.getString("aspect_cleanliness"));
        r.setAspectStaff(rs.getString("aspect_staff"));
        r.setAspectRoom(rs.getString("aspect_room"));
        r.setAspectFood(rs.getString("aspect_food"));
        r.setAspectValue(rs.getString("aspect_value"));
        r.setKeyHighlights(rs.getString("key_highlights"));
        r.setAiReplyDraft(rs.getString("ai_reply_draft"));
        r.setManagerReply(rs.getString("manager_reply"));
        r.setEscalated(rs.getBoolean("is_escalated"));
        r.setCreatedAt(rs.getTimestamp("created_at"));

        r.setGuestName(rs.getString("guest_name"));
        r.setBookingCode(rs.getString("booking_code"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setTypeName(rs.getString("type_name"));
        return r;
    }
}
