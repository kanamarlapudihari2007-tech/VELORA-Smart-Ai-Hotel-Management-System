package com.hotel.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Guest Model - Represents guest profile data from the 'guests' database table.
 */
public class Guest implements Serializable {

    private int id;
    private int userId;
    private String fullName;
    private String phone;
    private String idProof;
    private String address;
    private Timestamp createdAt;

    public Guest() {
    }

    public Guest(int id, int userId, String fullName, String phone, String idProof, String address, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.idProof = idProof;
        this.address = address;
        this.createdAt = createdAt;
    }

    public Guest(int userId, String fullName, String phone, String idProof, String address) {
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.idProof = idProof;
        this.address = address;
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

    public String getIdProof() {
        return idProof;
    }

    public void setIdProof(String idProof) {
        this.idProof = idProof;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
