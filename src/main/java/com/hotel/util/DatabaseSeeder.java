package com.hotel.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseSeeder - Resets and seeds clean demo data for College Viva & Project Defense demonstrations.
 * Part of Stage 20: College Viva & Project Defense Kit.
 */
public class DatabaseSeeder {

    /**
     * Resets rooms, bookings, tasks, complaints, reviews, and payments to a pristine demo state.
     * Preserves core user accounts, staff, and room types.
     */
    public static boolean resetAndSeedDemoData() {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                // 1. Temporarily disable foreign keys for clean table reset
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

                stmt.execute("DELETE FROM reviews");
                stmt.execute("DELETE FROM notifications");
                stmt.execute("DELETE FROM tasks");
                stmt.execute("DELETE FROM complaints");
                stmt.execute("DELETE FROM service_requests");
                stmt.execute("DELETE FROM payments");
                stmt.execute("DELETE FROM bookings");

                // Reset room status and room prices
                stmt.execute("UPDATE room_types SET price_per_night = 1200.00 WHERE type_name LIKE '%Single%'");
                stmt.execute("UPDATE room_types SET price_per_night = 2500.00 WHERE type_name LIKE '%Double%'");
                stmt.execute("UPDATE room_types SET price_per_night = 5000.00 WHERE type_name LIKE '%Suite%'");

                stmt.execute("UPDATE rooms SET status = 'AVAILABLE'");

                // Reset staff status and performance credits
                stmt.execute("UPDATE staff SET work_status = 'AVAILABLE', performance_credits = 100");

                // Re-enable foreign keys
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            }

            // 2. Seed past completed & utilized booking 100 for Alex Johnson (Guest ID 1, Room 2 - 102)
            String insertBooking100 = "INSERT INTO bookings (id, booking_code, guest_id, room_id, check_in_date, check_out_date, status, total_amount, created_at) " +
                                      "VALUES (100, 'BK-VIVA-100', 1, 2, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'CHECKED_OUT', 2500.00, DATE_SUB(NOW(), INTERVAL 5 DAY))";
            try (PreparedStatement ps = conn.prepareStatement(insertBooking100)) {
                ps.executeUpdate();
            }

            // Seed payment record for booking 100
            String insertPayment100 = "INSERT INTO payments (id, booking_id, amount, payment_method, payment_status, payment_date) " +
                                       "VALUES (100, 100, 2500.00, 'CARD', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 5 DAY))";
            try (PreparedStatement ps = conn.prepareStatement(insertPayment100)) {
                ps.executeUpdate();
            }

            // 3. Seed pristine active in-stay booking for Alex Johnson (Guest ID 1, Room 3 - 201)
            String insertBooking = "INSERT INTO bookings (id, booking_code, guest_id, room_id, check_in_date, check_out_date, status, total_amount, created_at) " +
                                  "VALUES (101, 'BK-VIVA-101', 1, 3, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'CHECKED_IN', 5000.00, NOW())";
            try (PreparedStatement ps = conn.prepareStatement(insertBooking)) {
                ps.executeUpdate();
            }

            // Mark room 201 as OCCUPIED
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("UPDATE rooms SET status = 'OCCUPIED' WHERE room_number = '201'");
            }

            // 4. Seed payment record for active booking 101
            String insertPayment = "INSERT INTO payments (id, booking_id, amount, payment_method, payment_status, payment_date) " +
                                   "VALUES (101, 101, 5000.00, 'CARD', 'COMPLETED', NOW())";
            try (PreparedStatement ps = conn.prepareStatement(insertPayment)) {
                ps.executeUpdate();
            }

            // 5. Seed sample service request for active booking 101
            String insertServiceReq = "INSERT INTO service_requests (id, booking_id, room_id, request_type, raw_text, " +
                                      "extracted_items, priority, status, created_at) " +
                                      "VALUES (201, 101, 3, 'Housekeeping', 'Can we please have 2 extra bath towels and a bottle of sparkling water?', " +
                                      "'2x Extra Bath Towels, 1x Sparkling Water', 'HIGH', 'IN_PROGRESS', NOW())";
            try (PreparedStatement ps = conn.prepareStatement(insertServiceReq)) {
                ps.executeUpdate();
            }

            // 6. Seed sample operational task
            String insertTask = "INSERT INTO tasks (id, task_type, reference_id, assigned_staff_id, priority, status, notes, created_at) " +
                                "VALUES (301, 'SERVICE_REQUEST', 201, 1, 'HIGH', 'IN_PROGRESS', " +
                                "'Deliver 2 extra towels and sparkling water to Room 201', NOW())";
            try (PreparedStatement ps = conn.prepareStatement(insertTask)) {
                ps.executeUpdate();
            }

            // Set staff 1 (Housekeeping) to BUSY
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("UPDATE staff SET work_status = 'BUSY' WHERE id = 1");
            }

            // 7. Seed sample guest complaint for active booking 101
            String insertComplaint = "INSERT INTO complaints (id, booking_id, room_id, raw_text, category, " +
                                     "priority, short_description, status, created_at) " +
                                     "VALUES (401, 101, 3, 'The AC unit is blowing lukewarm air and making a low buzzing noise.', " +
                                     "'Maintenance', 'HIGH', 'AC blowing lukewarm air with buzzing noise', 'IN_PROGRESS', NOW())";
            try (PreparedStatement ps = conn.prepareStatement(insertComplaint)) {
                ps.executeUpdate();
            }

            // 8. Seed sample verified immutable review for past completed & utilized booking 100
            String insertReview1 = "INSERT INTO reviews (id, booking_id, guest_id, rating, review_text, sentiment, " +
                                   "sentiment_score, aspect_cleanliness, aspect_staff, aspect_room, aspect_food, aspect_value, " +
                                   "key_highlights, ai_reply_draft, is_escalated, created_at) " +
                                   "VALUES (501, 100, 1, 5, 'Exceptional experience! The staff was courteous, room 102 was sparkling clean, and breakfast was superb.', " +
                                   "'POSITIVE', 0.95, 'POSITIVE', 'POSITIVE', 'POSITIVE', 'POSITIVE', 'POSITIVE', " +
                                   "'Impeccable cleanliness, attentive staff, and delicious breakfast.', " +
                                   "'Thank you for your generous praise of our team and breakfast service!', 0, DATE_SUB(NOW(), INTERVAL 3 DAY))";
            try (PreparedStatement ps = conn.prepareStatement(insertReview1)) {
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("[DatabaseSeeder] Successfully reset and seeded clean demo data for Viva Defense!");
            return true;

        } catch (SQLException e) {
            System.err.println("[DatabaseSeeder] Error resetting and seeding demo data: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("[DatabaseSeeder] Rollback error: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }
}
