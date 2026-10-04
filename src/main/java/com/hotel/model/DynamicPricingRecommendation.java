package com.hotel.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DynamicPricingRecommendation - Represents an AI pricing adjustment proposal for a room category.
 * Part of Stage 19: AI Dynamic Room Pricing & Revenue Optimization Engine (RMS).
 */
public class DynamicPricingRecommendation implements Serializable {

    private int roomTypeId;
    private String typeName;
    private BigDecimal currentPrice;
    private BigDecimal recommendedPrice;
    private double adjustmentPercent;
    private String strategy; // 'SURGE_PRICING', 'OPTIMAL_EQUILIBRIUM', 'DEMAND_STIMULATION'
    private String economicRationale;
    private int totalRooms;
    private int occupiedRooms;
    private double occupancyRate;

    public DynamicPricingRecommendation() {
    }

    public DynamicPricingRecommendation(int roomTypeId, String typeName, BigDecimal currentPrice,
                                        BigDecimal recommendedPrice, double adjustmentPercent,
                                        String strategy, String economicRationale) {
        this.roomTypeId = roomTypeId;
        this.typeName = typeName;
        this.currentPrice = currentPrice;
        this.recommendedPrice = recommendedPrice;
        this.adjustmentPercent = adjustmentPercent;
        this.strategy = strategy;
        this.economicRationale = economicRationale;
    }

    public int getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(int roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getRecommendedPrice() {
        return recommendedPrice;
    }

    public void setRecommendedPrice(BigDecimal recommendedPrice) {
        this.recommendedPrice = recommendedPrice;
    }

    public double getAdjustmentPercent() {
        return adjustmentPercent;
    }

    public void setAdjustmentPercent(double adjustmentPercent) {
        this.adjustmentPercent = adjustmentPercent;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getEconomicRationale() {
        return economicRationale;
    }

    public void setEconomicRationale(String economicRationale) {
        this.economicRationale = economicRationale;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public int getOccupiedRooms() {
        return occupiedRooms;
    }

    public void setOccupiedRooms(int occupiedRooms) {
        this.occupiedRooms = occupiedRooms;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }
}
