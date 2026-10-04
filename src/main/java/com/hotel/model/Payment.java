package com.hotel.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Payment Model - Represents transaction logs from 'payments' table.
 */
public class Payment implements Serializable {

    private int id;
    private int bookingId;
    private BigDecimal amount;
    private String paymentMethod; // 'CARD', 'CASH', 'UPI', 'NETBANKING'
    private String paymentStatus; // 'PENDING', 'COMPLETED', 'FAILED'
    private Timestamp paymentDate;

    public Payment() {
    }

    public Payment(int id, int bookingId, BigDecimal amount, String paymentMethod, String paymentStatus, Timestamp paymentDate) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
    }

    public Payment(int bookingId, BigDecimal amount, String paymentMethod, String paymentStatus) {
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Timestamp getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Timestamp paymentDate) {
        this.paymentDate = paymentDate;
    }
}
