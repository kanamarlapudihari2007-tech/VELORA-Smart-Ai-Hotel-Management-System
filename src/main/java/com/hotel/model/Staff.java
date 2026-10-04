package com.hotel.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Staff Model - Represents hotel staff profile information from the 'staff' table.
 */
public class Staff implements Serializable {

    private int id;
    private int userId;
    private String fullName;
    private String phone;
    private String category; // 'Housekeeping', 'Maintenance', 'Reception', 'Food Service', 'Security'
    private String workStatus; // 'AVAILABLE', 'BUSY', 'OFF_DUTY'
    private int performanceCredits; // Performance credits earned from on-time task completions
    private Timestamp createdAt;

    // Joined helper fields
    private String username;
    private String email;
    private int activeTaskCount; // Current active workload (ASSIGNED + IN_PROGRESS)

    public Staff() {
        this.performanceCredits = 100; // Default base credit score
    }

    public Staff(int id, int userId, String fullName, String phone, String category, String workStatus, int performanceCredits, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.category = category;
        this.workStatus = workStatus;
        this.performanceCredits = performanceCredits;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public int getPerformanceCredits() {
        return performanceCredits;
    }

    public void setPerformanceCredits(int performanceCredits) {
        this.performanceCredits = performanceCredits;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getActiveTaskCount() {
        return activeTaskCount;
    }

    public void setActiveTaskCount(int activeTaskCount) {
        this.activeTaskCount = activeTaskCount;
    }
}
