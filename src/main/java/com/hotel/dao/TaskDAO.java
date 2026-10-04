package com.hotel.dao;

import com.hotel.model.Task;
import com.hotel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TaskDAO - Data Access Object for handling staff task assignment, transitions, and resolution.
 */
public class TaskDAO {

    private final StaffDAO staffDAO = new StaffDAO();

    /**
     * Atomically assigns a task to an employee if it is unassigned or assigned to the same employee.
     * Prevents race conditions and double-assignment (Rule #11 Task Exclusivity).
     */
    public boolean assignTaskAtomic(int taskId, int staffId) {
        String sql = "UPDATE tasks SET assigned_staff_id = ?, status = 'ASSIGNED', updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ? AND (assigned_staff_id IS NULL OR assigned_staff_id = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, staffId);
            stmt.setInt(2, taskId);
            stmt.setInt(3, staffId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in assignTaskAtomic: " + e.getMessage());
            return false;
        }
    }

    /**
     * Clears the assigned employee from a task (e.g. upon decline or manager reset).
     */
    public boolean clearTaskAssignment(int taskId) {
        String sql = "UPDATE tasks SET assigned_staff_id = NULL, status = 'ASSIGNED', updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error clearing task assignment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all tasks specifically assigned to a staff member.
     */
    public List<Task> getTasksForStaff(int staffId, String category) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.id, t.task_type, t.reference_id, t.assigned_staff_id, t.priority, t.status, t.notes, " +
                     "t.created_at, t.updated_at, st.full_name AS staff_name, st.category AS staff_category, " +
                     "CASE " +
                     "    WHEN t.task_type = 'ROOM_CLEANING' THEN r_clean.room_number " +
                     "    WHEN t.task_type = 'SERVICE_REQUEST' THEN r_sr.room_number " +
                     "    WHEN t.task_type = 'COMPLAINT' THEN r_comp.room_number " +
                     "    ELSE 'N/A' " +
                     "END AS room_number, " +
                     "CASE " +
                     "    WHEN t.task_type = 'ROOM_CLEANING' THEN r_clean.id " +
                     "    WHEN t.task_type = 'SERVICE_REQUEST' THEN r_sr.id " +
                     "    WHEN t.task_type = 'COMPLAINT' THEN r_comp.id " +
                     "    ELSE 0 " +
                     "END AS room_id " +
                     "FROM tasks t " +
                     "LEFT JOIN staff st ON t.assigned_staff_id = st.id " +
                     "LEFT JOIN rooms r_clean ON (t.task_type = 'ROOM_CLEANING' AND t.reference_id = r_clean.id) " +
                     "LEFT JOIN service_requests sr ON (t.task_type = 'SERVICE_REQUEST' AND t.reference_id = sr.id) " +
                     "LEFT JOIN rooms r_sr ON (sr.room_id = r_sr.id) " +
                     "LEFT JOIN complaints c ON (t.task_type = 'COMPLAINT' AND t.reference_id = c.id) " +
                     "LEFT JOIN rooms r_comp ON (c.room_id = r_comp.id) " +
                     "WHERE t.assigned_staff_id = ? " +
                     "ORDER BY " +
                     "    CASE t.status WHEN 'IN_PROGRESS' THEN 1 WHEN 'ASSIGNED' THEN 2 WHEN 'COMPLETED' THEN 3 END, " +
                     "    CASE t.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'NORMAL' THEN 3 WHEN 'LOW' THEN 4 END, " +
                     "    t.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, staffId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tasks for staff: " + e.getMessage());
        }
        return tasks;
    }

    /**
     * Retrieves all tasks in the hotel for Manager oversight.
     */
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.id, t.task_type, t.reference_id, t.assigned_staff_id, t.priority, t.status, t.notes, " +
                     "t.created_at, t.updated_at, st.full_name AS staff_name, st.category AS staff_category, " +
                     "CASE " +
                     "    WHEN t.task_type = 'ROOM_CLEANING' THEN r_clean.room_number " +
                     "    WHEN t.task_type = 'SERVICE_REQUEST' THEN r_sr.room_number " +
                     "    WHEN t.task_type = 'COMPLAINT' THEN r_comp.room_number " +
                     "    ELSE 'N/A' " +
                     "END AS room_number, " +
                     "CASE " +
                     "    WHEN t.task_type = 'ROOM_CLEANING' THEN r_clean.id " +
                     "    WHEN t.task_type = 'SERVICE_REQUEST' THEN r_sr.id " +
                     "    WHEN t.task_type = 'COMPLAINT' THEN r_comp.id " +
                     "    ELSE 0 " +
                     "END AS room_id " +
                     "FROM tasks t " +
                     "LEFT JOIN staff st ON t.assigned_staff_id = st.id " +
                     "LEFT JOIN rooms r_clean ON (t.task_type = 'ROOM_CLEANING' AND t.reference_id = r_clean.id) " +
                     "LEFT JOIN service_requests sr ON (t.task_type = 'SERVICE_REQUEST' AND t.reference_id = sr.id) " +
                     "LEFT JOIN rooms r_sr ON (sr.room_id = r_sr.id) " +
                     "LEFT JOIN complaints c ON (t.task_type = 'COMPLAINT' AND t.reference_id = c.id) " +
                     "LEFT JOIN rooms r_comp ON (c.room_id = r_comp.id) " +
                     "ORDER BY " +
                     "    CASE WHEN t.assigned_staff_id IS NULL THEN 1 ELSE 2 END, " +
                     "    CASE t.status WHEN 'IN_PROGRESS' THEN 1 WHEN 'ASSIGNED' THEN 2 WHEN 'COMPLETED' THEN 3 END, " +
                     "    CASE t.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'NORMAL' THEN 3 WHEN 'LOW' THEN 4 END, " +
                     "    t.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all tasks: " + e.getMessage());
        }
        return tasks;
    }

    /**
     * Retrieves a single Task by ID with all joined details.
     */
    public Task getTaskById(int taskId) {
        String sql = "SELECT t.id, t.task_type, t.reference_id, t.assigned_staff_id, t.priority, t.status, t.notes, " +
                     "t.created_at, t.updated_at, st.full_name AS staff_name, st.category AS staff_category, " +
                     "CASE " +
                     "    WHEN t.task_type = 'ROOM_CLEANING' THEN r_clean.room_number " +
                     "    WHEN t.task_type = 'SERVICE_REQUEST' THEN r_sr.room_number " +
                     "    WHEN t.task_type = 'COMPLAINT' THEN r_comp.room_number " +
                     "    ELSE 'N/A' " +
                     "END AS room_number, " +
                     "CASE " +
                     "    WHEN t.task_type = 'ROOM_CLEANING' THEN r_clean.id " +
                     "    WHEN t.task_type = 'SERVICE_REQUEST' THEN r_sr.id " +
                     "    WHEN t.task_type = 'COMPLAINT' THEN r_comp.id " +
                     "    ELSE 0 " +
                     "END AS room_id " +
                     "FROM tasks t " +
                     "LEFT JOIN staff st ON t.assigned_staff_id = st.id " +
                     "LEFT JOIN rooms r_clean ON (t.task_type = 'ROOM_CLEANING' AND t.reference_id = r_clean.id) " +
                     "LEFT JOIN service_requests sr ON (t.task_type = 'SERVICE_REQUEST' AND t.reference_id = sr.id) " +
                     "LEFT JOIN rooms r_sr ON (sr.room_id = r_sr.id) " +
                     "LEFT JOIN complaints c ON (t.task_type = 'COMPLAINT' AND t.reference_id = c.id) " +
                     "LEFT JOIN rooms r_comp ON (c.room_id = r_comp.id) " +
                     "WHERE t.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTask(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching task by id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Calculates task metrics for a staff member (Assigned, In Progress, Completed).
     */
    public Map<String, Integer> getStaffTaskStats(int staffId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("assigned", 0);
        stats.put("inProgress", 0);
        stats.put("completed", 0);

        String sql = "SELECT status, COUNT(*) AS cnt FROM tasks WHERE assigned_staff_id = ? GROUP BY status";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, staffId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("cnt");
                    if ("ASSIGNED".equalsIgnoreCase(status)) stats.put("assigned", count);
                    else if ("IN_PROGRESS".equalsIgnoreCase(status)) stats.put("inProgress", count);
                    else if ("COMPLETED".equalsIgnoreCase(status)) stats.put("completed", count);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating staff task stats: " + e.getMessage());
        }
        return stats;
    }

    /**
     * Accepts a task: sets status to 'IN_PROGRESS', marks staff work_status as 'BUSY',
     * and synchronizes request/complaint status.
     */
    public boolean acceptTask(int taskId, int staffId) {
        Task task = getTaskById(taskId);
        if (task == null || "COMPLETED".equals(task.getStatus())) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Update task to IN_PROGRESS and confirm assignment to staff
            String sqlTask = "UPDATE tasks SET assigned_staff_id = ?, status = 'IN_PROGRESS', updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement stmtT = conn.prepareStatement(sqlTask)) {
                stmtT.setInt(1, staffId);
                stmtT.setInt(2, taskId);
                stmtT.executeUpdate();
            }

            // 2. Set staff member to BUSY
            String sqlStaff = "UPDATE staff SET work_status = 'BUSY' WHERE id = ?";
            try (PreparedStatement stmtS = conn.prepareStatement(sqlStaff)) {
                stmtS.setInt(1, staffId);
                stmtS.executeUpdate();
            }

            // 3. Synchronize underlying source status if Service Request or Complaint
            if ("SERVICE_REQUEST".equals(task.getTaskType())) {
                String sqlSR = "UPDATE service_requests SET status = 'IN_PROGRESS' WHERE id = ?";
                try (PreparedStatement stmtSR = conn.prepareStatement(sqlSR)) {
                    stmtSR.setInt(1, task.getReferenceId());
                    stmtSR.executeUpdate();
                }
            } else if ("COMPLAINT".equals(task.getTaskType())) {
                String sqlC = "UPDATE complaints SET status = 'IN_PROGRESS' WHERE id = ?";
                try (PreparedStatement stmtC = conn.prepareStatement(sqlC)) {
                    stmtC.setInt(1, task.getReferenceId());
                    stmtC.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error accepting task: " + e.getMessage());
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    /**
     * Completes a task: transitions status to 'COMPLETED', frees the staff member (AVAILABLE),
     * awards performance credits, and executes hotel domain updates (e.g. cleans room -> AVAILABLE).
     */
    public boolean completeTask(int taskId, int staffId) {
        Task task = getTaskById(taskId);
        if (task == null || "COMPLETED".equals(task.getStatus())) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Mark task as COMPLETED
            String sqlTask = "UPDATE tasks SET status = 'COMPLETED', updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement stmtT = conn.prepareStatement(sqlTask)) {
                stmtT.setInt(1, taskId);
                stmtT.executeUpdate();
            }

            // 2. Check if staff has any other active tasks; if none, set to AVAILABLE
            String sqlActiveCheck = "SELECT COUNT(*) FROM tasks WHERE assigned_staff_id = ? AND status = 'IN_PROGRESS' AND id != ?";
            boolean hasOtherActive = false;
            try (PreparedStatement stmtAC = conn.prepareStatement(sqlActiveCheck)) {
                stmtAC.setInt(1, staffId);
                stmtAC.setInt(2, taskId);
                try (ResultSet rsAC = stmtAC.executeQuery()) {
                    if (rsAC.next() && rsAC.getInt(1) > 0) {
                        hasOtherActive = true;
                    }
                }
            }

            if (!hasOtherActive) {
                String sqlStaff = "UPDATE staff SET work_status = 'AVAILABLE' WHERE id = ?";
                try (PreparedStatement stmtS = conn.prepareStatement(sqlStaff)) {
                    stmtS.setInt(1, staffId);
                    stmtS.executeUpdate();
                }
            }

            // 3. Domain Logic by Task Type:
            if ("ROOM_CLEANING".equals(task.getTaskType())) {
                // Room has been cleaned -> change room status back to AVAILABLE
                String sqlRoom = "UPDATE rooms SET status = 'AVAILABLE' WHERE id = ? AND status = 'CLEANING'";
                try (PreparedStatement stmtR = conn.prepareStatement(sqlRoom)) {
                    stmtR.setInt(1, task.getReferenceId());
                    stmtR.executeUpdate();
                }
            } else if ("SERVICE_REQUEST".equals(task.getTaskType())) {
                String sqlSR = "UPDATE service_requests SET status = 'COMPLETED' WHERE id = ?";
                try (PreparedStatement stmtSR = conn.prepareStatement(sqlSR)) {
                    stmtSR.setInt(1, task.getReferenceId());
                    stmtSR.executeUpdate();
                }
            } else if ("COMPLAINT".equals(task.getTaskType())) {
                String sqlC = "UPDATE complaints SET status = 'RESOLVED' WHERE id = ?";
                try (PreparedStatement stmtC = conn.prepareStatement(sqlC)) {
                    stmtC.setInt(1, task.getReferenceId());
                    stmtC.executeUpdate();
                }

                // If room was in MAINTENANCE, check if any open complaints remain
                if (task.getRoomId() > 0) {
                    String sqlCheck = "SELECT COUNT(*) FROM complaints WHERE room_id = ? AND status != 'RESOLVED'";
                    boolean hasPendingComplaints = false;
                    try (PreparedStatement stmtChk = conn.prepareStatement(sqlCheck)) {
                        stmtChk.setInt(1, task.getRoomId());
                        try (ResultSet rsChk = stmtChk.executeQuery()) {
                            if (rsChk.next() && rsChk.getInt(1) > 0) {
                                hasPendingComplaints = true;
                            }
                        }
                    }
                    if (!hasPendingComplaints) {
                        String sqlRoomMaint = "UPDATE rooms SET status = 'AVAILABLE' WHERE id = ? AND status = 'MAINTENANCE'";
                        try (PreparedStatement stmtRM = conn.prepareStatement(sqlRoomMaint)) {
                            stmtRM.setInt(1, task.getRoomId());
                            stmtRM.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();

            // 4. Award performance credits based on task priority
            int creditAward = 10;
            if ("URGENT".equalsIgnoreCase(task.getPriority())) creditAward = 20;
            else if ("HIGH".equalsIgnoreCase(task.getPriority())) creditAward = 15;
            else if ("LOW".equalsIgnoreCase(task.getPriority())) creditAward = 5;

            staffDAO.adjustPerformanceCredits(staffId, creditAward);

            return true;
        } catch (SQLException e) {
            System.err.println("Error completing task: " + e.getMessage());
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getInt("id"));
        t.setTaskType(rs.getString("task_type"));
        t.setReferenceId(rs.getInt("reference_id"));

        int staffId = rs.getInt("assigned_staff_id");
        if (!rs.wasNull()) {
            t.setAssignedStaffId(staffId);
        } else {
            t.setAssignedStaffId(null);
        }

        t.setPriority(rs.getString("priority"));
        t.setStatus(rs.getString("status"));
        t.setNotes(rs.getString("notes"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setUpdatedAt(rs.getTimestamp("updated_at"));

        t.setStaffName(rs.getString("staff_name"));
        t.setStaffCategory(rs.getString("staff_category"));
        t.setRoomNumber(rs.getString("room_number"));
        t.setRoomId(rs.getInt("room_id"));
        return t;
    }
}
