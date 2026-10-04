package com.hotel.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Room Model - Represents physical hotel rooms from 'rooms' table with joined RoomType info.
 */
public class Room implements Serializable {

    private int id;
    private String roomNumber;
    private int roomTypeId;
    private int floorNumber;
    private String status; // 'AVAILABLE', 'RESERVED', 'OCCUPIED', 'CLEANING', 'MAINTENANCE'

    // Joined fields from RoomType for easy view rendering
    private String typeName;
    private BigDecimal pricePerNight;
    private int capacity;

    public Room() {
    }

    public Room(int id, String roomNumber, int roomTypeId, int floorNumber, String status) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomTypeId = roomTypeId;
        this.floorNumber = floorNumber;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(int roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
