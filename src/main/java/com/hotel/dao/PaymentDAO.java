package com.hotel.dao;

import com.hotel.model.Payment;
import com.hotel.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PaymentDAO - Data Access Object for transaction history and billing settlements.
 */
public class PaymentDAO {

    /**
     * Retrieves the primary or most recent payment associated with a booking.
     */
    public Payment getPaymentByBookingId(int bookingId) {
        String sql = "SELECT id, booking_id, amount, payment_method, payment_status, payment_date " +
                     "FROM payments WHERE booking_id = ? ORDER BY id DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Payment(
                        rs.getInt("id"),
                        rs.getInt("booking_id"),
                        rs.getBigDecimal("amount"),
                        rs.getString("payment_method"),
                        rs.getString("payment_status"),
                        rs.getTimestamp("payment_date")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payment by booking_id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all payments recorded for a booking (e.g. advance + extras).
     */
    public List<Payment> getAllPaymentsForBooking(int bookingId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT id, booking_id, amount, payment_method, payment_status, payment_date " +
                     "FROM payments WHERE booking_id = ? ORDER BY payment_date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(new Payment(
                        rs.getInt("id"),
                        rs.getInt("booking_id"),
                        rs.getBigDecimal("amount"),
                        rs.getString("payment_method"),
                        rs.getString("payment_status"),
                        rs.getTimestamp("payment_date")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payments: " + e.getMessage());
        }
        return payments;
    }

    /**
     * Inserts a new payment transaction record.
     */
    public boolean recordPayment(int bookingId, BigDecimal amount, String method, String status) {
        String sql = "INSERT INTO payments (booking_id, amount, payment_method, payment_status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            stmt.setBigDecimal(2, amount);
            stmt.setString(3, method != null ? method : "CARD");
            stmt.setString(4, status != null ? status : "COMPLETED");

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error recording payment: " + e.getMessage());
            return false;
        }
    }
}
