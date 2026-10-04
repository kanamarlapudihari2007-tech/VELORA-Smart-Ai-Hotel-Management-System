package com.hotel.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * ServiceRequest Model - Represents room service & housekeeping orders from 'service_requests' table.
 */
public class ServiceRequest implements Serializable {

    private int id;
    private int bookingId;
    private int roomId;
    private String requestType; // 'Housekeeping', 'Food Service', 'Maintenance', 'General'
    private String rawText;
    private String extractedItems;
    private String priority; // 'LOW', 'NORMAL', 'HIGH', 'URGENT'
    private String status;   // 'PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'
    private Timestamp createdAt;

    // Joined fields for view rendering
    private String roomNumber;
    private String guestName;
    private String bookingCode;

    public ServiceRequest() {
    }

    public ServiceRequest(int id, int bookingId, int roomId, String requestType, String rawText, String extractedItems, String priority, String status, Timestamp createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.requestType = requestType;
        this.rawText = rawText;
        this.extractedItems = extractedItems;
        this.priority = priority;
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

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getExtractedItems() {
        return extractedItems;
    }

    public void setExtractedItems(String extractedItems) {
        this.extractedItems = extractedItems;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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
