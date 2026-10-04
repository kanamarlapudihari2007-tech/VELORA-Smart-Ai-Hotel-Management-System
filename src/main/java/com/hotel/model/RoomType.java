package com.hotel.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * RoomType Model - Represents room categories from 'room_types' table.
 */
public class RoomType implements Serializable {

    private int id;
    private String typeName;
    private BigDecimal pricePerNight;
    private String description;
    private int capacity;

    public RoomType() {
    }

    public RoomType(int id, String typeName, BigDecimal pricePerNight, String description, int capacity) {
        this.id = id;
        this.typeName = typeName;
        this.pricePerNight = pricePerNight;
        this.description = description;
        this.capacity = capacity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
