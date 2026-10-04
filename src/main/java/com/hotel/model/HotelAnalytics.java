package com.hotel.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HotelAnalytics - Data model holding comprehensive operational, financial,
 * occupancy, and AI executive intelligence metrics for Stage 17.
 */
public class HotelAnalytics implements Serializable {

    private static final long serialVersionUID = 1L;

    // 1. Room Occupancy
    private int totalRooms;
    private int occupiedRooms;
    private int availableRooms;
    private int reservedRooms;
    private int cleaningRooms;
    private int maintenanceRooms;
    private double occupancyRate; // e.g. 75.0%

    // 2. Financial Metrics
    private double totalRevenue;
    private double paidRevenue;
    private double pendingRevenue;
    private int totalBookingsCount;
    private int activeCheckInsCount;

    // 3. Operational Issues & Complaints
    private int totalComplaints;
    private int pendingComplaints;
    private int inProgressComplaints;
    private int resolvedComplaints;
    private int urgentComplaints;
    private Map<String, Integer> complaintsByCategory = new HashMap<>();

    // 4. Tasks & Staff Workload
    private int totalTasks;
    private int completedTasks;
    private int pendingTasks;
    private int inProgressTasks;
    private int totalStaffCount;
    private int availableStaffCount;
    private int busyStaffCount;
    private List<Staff> topStaff;

    // 5. Google Gemini AI Executive Briefing
    private String aiExecutiveHeadline;
    private String aiOperationalBriefing;
    private String aiRiskAlerts;
    private String aiActionableRecommendations;
    private boolean fromAI;
    private String generatedTimestamp;

    public HotelAnalytics() {}

    // Getters and Setters
    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public int getOccupiedRooms() { return occupiedRooms; }
    public void setOccupiedRooms(int occupiedRooms) { this.occupiedRooms = occupiedRooms; }

    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }

    public int getReservedRooms() { return reservedRooms; }
    public void setReservedRooms(int reservedRooms) { this.reservedRooms = reservedRooms; }

    public int getCleaningRooms() { return cleaningRooms; }
    public void setCleaningRooms(int cleaningRooms) { this.cleaningRooms = cleaningRooms; }

    public int getMaintenanceRooms() { return maintenanceRooms; }
    public void setMaintenanceRooms(int maintenanceRooms) { this.maintenanceRooms = maintenanceRooms; }

    public double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(double occupancyRate) { this.occupancyRate = occupancyRate; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getPaidRevenue() { return paidRevenue; }
    public void setPaidRevenue(double paidRevenue) { this.paidRevenue = paidRevenue; }

    public double getPendingRevenue() { return pendingRevenue; }
    public void setPendingRevenue(double pendingRevenue) { this.pendingRevenue = pendingRevenue; }

    public int getTotalBookingsCount() { return totalBookingsCount; }
    public void setTotalBookingsCount(int totalBookingsCount) { this.totalBookingsCount = totalBookingsCount; }

    public int getActiveCheckInsCount() { return activeCheckInsCount; }
    public void setActiveCheckInsCount(int activeCheckInsCount) { this.activeCheckInsCount = activeCheckInsCount; }

    public int getTotalComplaints() { return totalComplaints; }
    public void setTotalComplaints(int totalComplaints) { this.totalComplaints = totalComplaints; }

    public int getPendingComplaints() { return pendingComplaints; }
    public void setPendingComplaints(int pendingComplaints) { this.pendingComplaints = pendingComplaints; }

    public int getInProgressComplaints() { return inProgressComplaints; }
    public void setInProgressComplaints(int inProgressComplaints) { this.inProgressComplaints = inProgressComplaints; }

    public int getResolvedComplaints() { return resolvedComplaints; }
    public void setResolvedComplaints(int resolvedComplaints) { this.resolvedComplaints = resolvedComplaints; }

    public int getUrgentComplaints() { return urgentComplaints; }
    public void setUrgentComplaints(int urgentComplaints) { this.urgentComplaints = urgentComplaints; }

    public Map<String, Integer> getComplaintsByCategory() { return complaintsByCategory; }
    public void setComplaintsByCategory(Map<String, Integer> complaintsByCategory) { this.complaintsByCategory = complaintsByCategory; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }

    public int getInProgressTasks() { return inProgressTasks; }
    public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }

    public int getTotalStaffCount() { return totalStaffCount; }
    public void setTotalStaffCount(int totalStaffCount) { this.totalStaffCount = totalStaffCount; }

    public int getAvailableStaffCount() { return availableStaffCount; }
    public void setAvailableStaffCount(int availableStaffCount) { this.availableStaffCount = availableStaffCount; }

    public int getBusyStaffCount() { return busyStaffCount; }
    public void setBusyStaffCount(int busyStaffCount) { this.busyStaffCount = busyStaffCount; }

    public List<Staff> getTopStaff() { return topStaff; }
    public void setTopStaff(List<Staff> topStaff) { this.topStaff = topStaff; }

    public String getAiExecutiveHeadline() { return aiExecutiveHeadline; }
    public void setAiExecutiveHeadline(String aiExecutiveHeadline) { this.aiExecutiveHeadline = aiExecutiveHeadline; }

    public String getAiOperationalBriefing() { return aiOperationalBriefing; }
    public void setAiOperationalBriefing(String aiOperationalBriefing) { this.aiOperationalBriefing = aiOperationalBriefing; }

    public String getAiRiskAlerts() { return aiRiskAlerts; }
    public void setAiRiskAlerts(String aiRiskAlerts) { this.aiRiskAlerts = aiRiskAlerts; }

    public String getAiActionableRecommendations() { return aiActionableRecommendations; }
    public void setAiActionableRecommendations(String aiActionableRecommendations) { this.aiActionableRecommendations = aiActionableRecommendations; }

    public boolean isFromAI() { return fromAI; }
    public void setFromAI(boolean fromAI) { this.fromAI = fromAI; }

    public String getGeneratedTimestamp() { return generatedTimestamp; }
    public void setGeneratedTimestamp(String generatedTimestamp) { this.generatedTimestamp = generatedTimestamp; }
}
