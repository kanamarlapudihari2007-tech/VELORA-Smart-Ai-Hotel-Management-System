package com.hotel.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Notification Model - Represents real-time alerts and assignment messages for users.
 */
public class Notification implements Serializable {

    private int id;
    private int userId;
    private String title;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {
    }

    public Notification(int id, int userId, String title, String message, boolean isRead, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public Notification(int userId, String title, String message) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.isRead = false;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
