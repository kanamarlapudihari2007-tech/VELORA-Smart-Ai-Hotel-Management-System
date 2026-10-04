package com.hotel.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * RevenueOptimizationReport - Comprehensive AI Revenue Management System (RMS) Report.
 * Encapsulates live occupancy, market demand indicators, and room pricing recommendations.
 */
public class RevenueOptimizationReport implements Serializable {

    private double overallOccupancyRate;
    private int totalRooms;
    private int occupiedRooms;
    private int availableRooms;
    private String dayOfWeek;
    private boolean isWeekend;
    private String overallStrategyHeadline;
    private String aiMarketSummary;
    private List<DynamicPricingRecommendation> recommendations = new ArrayList<>();
    private boolean fromAI;
    private String generatedTimestamp;

    public RevenueOptimizationReport() {
    }

    public double getOverallOccupancyRate() {
        return overallOccupancyRate;
    }

    public void setOverallOccupancyRate(double overallOccupancyRate) {
        this.overallOccupancyRate = overallOccupancyRate;
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

    public int getAvailableRooms() {
        return availableRooms;
    }

    public void setAvailableRooms(int availableRooms) {
        this.availableRooms = availableRooms;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public boolean isWeekend() {
        return isWeekend;
    }

    public void setWeekend(boolean weekend) {
        isWeekend = weekend;
    }

    public String getOverallStrategyHeadline() {
        return overallStrategyHeadline;
    }

    public void setOverallStrategyHeadline(String overallStrategyHeadline) {
        this.overallStrategyHeadline = overallStrategyHeadline;
    }

    public String getAiMarketSummary() {
        return aiMarketSummary;
    }

    public void setAiMarketSummary(String aiMarketSummary) {
        this.aiMarketSummary = aiMarketSummary;
    }

    public List<DynamicPricingRecommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<DynamicPricingRecommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public boolean isFromAI() {
        return fromAI;
    }

    public void setFromAI(boolean fromAI) {
        this.fromAI = fromAI;
    }

    public String getGeneratedTimestamp() {
        return generatedTimestamp;
    }

    public void setGeneratedTimestamp(String generatedTimestamp) {
        this.generatedTimestamp = generatedTimestamp;
    }
}
