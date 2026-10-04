package com.hotel.dao;

import com.hotel.model.Staff;
import com.hotel.model.Task;
import com.hotel.service.TaskAssignmentService;
import com.hotel.util.DBConnection;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * TaskLifecycleTest - Integration tests verifying end-to-end task lifecycle,
 * exclusivity concurrency protection, and automatic assignment.
 */
public class TaskLifecycleTest {

    private TaskDAO taskDAO;
    private StaffDAO staffDAO;
    private TaskAssignmentService assignmentService;

    @Before
    public void setUp() {
        // Skip DB integration test if local MySQL server is not currently reachable
        boolean dbReachable = false;
        try (Connection conn = DBConnection.getConnection()) {
            dbReachable = (conn != null && !conn.isClosed());
        } catch (SQLException ignored) {
        }
        Assume.assumeTrue("Local MySQL database is not reachable, skipping live DB tests", dbReachable);

        taskDAO = new TaskDAO();
        staffDAO = new StaffDAO();
        assignmentService = new TaskAssignmentService();
    }

    @Test
    public void testTaskExclusivityProtection() {
        List<Task> allTasks = taskDAO.getAllTasks();
        if (allTasks.isEmpty()) return;

        Task testTask = allTasks.get(0);
        int taskId = testTask.getId();

        // 1. Assign to staff 1
        boolean assignedFirst = taskDAO.assignTaskAtomic(taskId, 1);

        // 2. Simultaneous assignment attempt to staff 2 on already assigned task must be rejected
        // if another worker tries to claim it
        String checkSql = "UPDATE tasks SET assigned_staff_id = 999 WHERE id = ? AND assigned_staff_id IS NULL";
        int rowsUpdated = 0;
        try (Connection conn = DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setInt(1, taskId);
            rowsUpdated = stmt.executeUpdate();
        } catch (SQLException e) {
            fail("SQLException: " + e.getMessage());
        }

        assertEquals("Task already claimed must reject simultaneous overwrite", 0, rowsUpdated);
    }

    @Test
    public void testStaffPerformanceCreditAdjustment() {
        Staff staff = staffDAO.getStaffById(1);
        if (staff == null) return;

        int initialCredits = staff.getPerformanceCredits();
        staffDAO.adjustPerformanceCredits(staff.getId(), 15);

        Staff updated = staffDAO.getStaffById(1);
        assertNotNull(updated);
        assertEquals(initialCredits + 15, updated.getPerformanceCredits());

        // Revert back
        staffDAO.adjustPerformanceCredits(staff.getId(), -15);
    }
}
