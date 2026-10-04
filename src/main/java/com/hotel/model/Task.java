package com.hotel.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Task Model - Represents work tasks assigned to staff members from the 'tasks' table.
 */
public class Task implements Serializable {

    private int id;
    private String taskType; // 'SERVICE_REQUEST', 'COMPLAINT', 'ROOM_CLEANING'
    private int referenceId;
    private Integer assignedStaffId;
    private String priority; // 'LOW', 'NORMAL', 'HIGH', 'URGENT'
    private String status; // 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED'
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Joined helper fields
    private String staffName;
    private String staffCategory;
    private String roomNumber;
    private int roomId;

    public Task() {
    }

    public Task(int id, String taskType, int referenceId, Integer assignedStaffId, String priority, String status, String notes, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.taskType = taskType;
        this.referenceId = referenceId;
        this.assignedStaffId = assignedStaffId;
        this.priority = priority;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }

    public Integer getAssignedStaffId() {
        return assignedStaffId;
    }

    public void setAssignedStaffId(Integer assignedStaffId) {
        this.assignedStaffId = assignedStaffId;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffCategory() {
        return staffCategory;
    }

    public void setStaffCategory(String staffCategory) {
        this.staffCategory = staffCategory;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
