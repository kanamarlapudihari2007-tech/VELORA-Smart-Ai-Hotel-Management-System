package com.hotel.dao;

import com.hotel.model.HotelAnalytics;
import com.hotel.model.Staff;
import com.hotel.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AnalyticsDAO - Stage 17 Data Access Object aggregating real-time hotel operational,
 * occupancy, financial, and departmental metrics.
 */
public class AnalyticsDAO {

    private final StaffDAO staffDAO;

    public AnalyticsDAO() {
        this.staffDAO = new StaffDAO();
    }

    public AnalyticsDAO(StaffDAO staffDAO) {
        this.staffDAO = (staffDAO != null) ? staffDAO : new StaffDAO();
    }

    /**
     * Gathers all real-time operational and business metrics across rooms, bookings,
     * complaints, tasks, and staff.
     */
    public HotelAnalytics getHotelAnalytics() {
        HotelAnalytics analytics = new HotelAnalytics();
        populateRoomMetrics(analytics);
        populateRevenueMetrics(analytics);
        populateComplaintMetrics(analytics);
        populateTaskAndStaffMetrics(analytics);
        return analytics;
    }

    private void populateRoomMetrics(HotelAnalytics a) {
        String sql = "SELECT status, COUNT(*) AS cnt FROM rooms GROUP BY status";
        int total = 0;
        int occupied = 0;
        int available = 0;
        int reserved = 0;
        int cleaning = 0;
        int maintenance = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String st = rs.getString("status");
                int count = rs.getInt("cnt");
                total += count;

                if ("OCCUPIED".equalsIgnoreCase(st)) {
                    occupied = count;
                } else if ("AVAILABLE".equalsIgnoreCase(st)) {
                    available = count;
                } else if ("RESERVED".equalsIgnoreCase(st)) {
                    reserved = count;
                } else if ("CLEANING".equalsIgnoreCase(st)) {
                    cleaning = count;
                } else if ("MAINTENANCE".equalsIgnoreCase(st)) {
                    maintenance = count;
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] Room metrics error: " + e.getMessage());
        }

        a.setTotalRooms(total);
        a.setOccupiedRooms(occupied);
        a.setAvailableRooms(available);
        a.setReservedRooms(reserved);
        a.setCleaningRooms(cleaning);
        a.setMaintenanceRooms(maintenance);

        double rate = (total > 0) ? (((double) (occupied + reserved) / total) * 100.0) : 0.0;
        a.setOccupancyRate(Math.round(rate * 10.0) / 10.0);
    }

    private void populateRevenueMetrics(HotelAnalytics a) {
        String bookingSql = "SELECT COUNT(*) AS total_b, COALESCE(SUM(total_amount), 0) AS total_rev, "
                          + "COUNT(CASE WHEN status = 'CHECKED_IN' THEN 1 END) AS active_ci "
                          + "FROM bookings WHERE status != 'CANCELLED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(bookingSql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                a.setTotalBookingsCount(rs.getInt("total_b"));
                a.setTotalRevenue(rs.getDouble("total_rev"));
                a.setActiveCheckInsCount(rs.getInt("active_ci"));
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] Bookings revenue metrics error: " + e.getMessage());
        }

        String paymentSql = "SELECT COALESCE(SUM(amount), 0) AS paid FROM payments WHERE payment_status = 'COMPLETED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(paymentSql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double paid = rs.getDouble("paid");
                a.setPaidRevenue(paid);
                double pending = Math.max(0.0, a.getTotalRevenue() - paid);
                a.setPendingRevenue(pending);
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] Payment metrics error: " + e.getMessage());
        }
    }

    private void populateComplaintMetrics(HotelAnalytics a) {
        String statusSql = "SELECT status, COUNT(*) AS cnt FROM complaints GROUP BY status";
        int total = 0;
        int pending = 0;
        int inProgress = 0;
        int resolved = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(statusSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String st = rs.getString("status");
                int count = rs.getInt("cnt");
                total += count;

                if ("PENDING".equalsIgnoreCase(st)) {
                    pending = count;
                } else if ("IN_PROGRESS".equalsIgnoreCase(st)) {
                    inProgress = count;
                } else if ("RESOLVED".equalsIgnoreCase(st)) {
                    resolved = count;
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] Complaint status metrics error: " + e.getMessage());
        }

        a.setTotalComplaints(total);
        a.setPendingComplaints(pending);
        a.setInProgressComplaints(inProgress);
        a.setResolvedComplaints(resolved);

        // Urgent / High complaints
        String urgentSql = "SELECT COUNT(*) AS cnt FROM complaints WHERE priority IN ('URGENT', 'HIGH') AND status != 'RESOLVED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(urgentSql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                a.setUrgentComplaints(rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] Urgent complaint metrics error: " + e.getMessage());
        }

        // Category breakdown
        String categorySql = "SELECT category, COUNT(*) AS cnt FROM complaints GROUP BY category";
        Map<String, Integer> catMap = new HashMap<>();
        catMap.put("Maintenance", 0);
        catMap.put("Housekeeping", 0);
        catMap.put("Food Service", 0);
        catMap.put("Reception", 0);
        catMap.put("Security", 0);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(categorySql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String cat = rs.getString("category");
                int cnt = rs.getInt("cnt");
                if (cat != null) {
                    catMap.put(cat, cnt);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] Complaint category breakdown error: " + e.getMessage());
        }
        a.setComplaintsByCategory(catMap);
    }

    private void populateTaskAndStaffMetrics(HotelAnalytics a) {
        String taskSql = "SELECT status, COUNT(*) AS cnt FROM tasks GROUP BY status";
        int total = 0;
        int pending = 0;
        int inProgress = 0;
        int completed = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(taskSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String st = rs.getString("status");
                int count = rs.getInt("cnt");
                total += count;

                if ("PENDING".equalsIgnoreCase(st)) {
                    pending = count;
                } else if ("IN_PROGRESS".equalsIgnoreCase(st)) {
                    inProgress = count;
                } else if ("COMPLETED".equalsIgnoreCase(st)) {
                    completed = count;
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] Task metrics error: " + e.getMessage());
        }

        a.setTotalTasks(total);
        a.setPendingTasks(pending);
        a.setInProgressTasks(inProgress);
        a.setCompletedTasks(completed);

        // Staff workload and leaderboard
        try {
            List<Staff> staffList = staffDAO.getAllStaff();
            if (staffList != null) {
                a.setTotalStaffCount(staffList.size());
                int avail = 0;
                int busy = 0;
                for (Staff s : staffList) {
                    if ("AVAILABLE".equalsIgnoreCase(s.getWorkStatus())) {
                        avail++;
                    } else if ("BUSY".equalsIgnoreCase(s.getWorkStatus())) {
                        busy++;
                    }
                }
                a.setAvailableStaffCount(avail);
                a.setBusyStaffCount(busy);

                // Sort by performance credits descending
                List<Staff> topStaff = new ArrayList<>(staffList);
                topStaff.sort((s1, s2) -> Integer.compare(s2.getPerformanceCredits(), s1.getPerformanceCredits()));
                if (topStaff.size() > 5) {
                    topStaff = topStaff.subList(0, 5);
                }
                a.setTopStaff(topStaff);
            }
        } catch (Exception e) {
            System.err.println("[AnalyticsDAO] Staff metrics error: " + e.getMessage());
        }
    }
}
