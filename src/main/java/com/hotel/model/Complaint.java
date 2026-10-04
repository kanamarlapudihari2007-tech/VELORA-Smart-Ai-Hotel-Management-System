package com.hotel.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Complaint Model - Represents issue complaints logged by guests from 'complaints' table.
 */
public class Complaint implements Serializable {

    private int id;
    private int bookingId;
    private int roomId;
    private String rawText;
    private String category; // e.g. 'Maintenance', 'Plumbing', 'Housekeeping', 'WiFi/Electrical'
    private String priority; // 'LOW', 'NORMAL', 'HIGH', 'URGENT'
    private String shortDescription;
    private String status; // 'PENDING', 'IN_PROGRESS', 'RESOLVED'
    private Timestamp createdAt;

    // Joined fields
    private String roomNumber;
    private String guestName;
    private String bookingCode;

    public Complaint() {
    }

    public Complaint(int id, int bookingId, int roomId, String rawText, String category, String priority, String shortDescription, String status, Timestamp createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.rawText = rawText;
        this.category = category;
        this.priority = priority;
        this.shortDescription = shortDescription;
        this.status = status;
        this.createdAt = createdAt;
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

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }
}
