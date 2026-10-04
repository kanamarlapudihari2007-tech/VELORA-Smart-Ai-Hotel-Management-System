package com.hotel.controller;

import com.hotel.dao.NotificationDAO;
import com.hotel.dao.StaffDAO;
import com.hotel.dao.TaskDAO;
import com.hotel.model.Notification;
import com.hotel.model.Staff;
import com.hotel.model.Task;
import com.hotel.model.User;
import com.hotel.service.TaskAssignmentService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * StaffTaskServlet - Handles staff task dashboard, task acceptance, decline/reassignment,
 * work duty toggling, and task completion.
 * Mapped to /staff-tasks
 */
@WebServlet("/staff-tasks")
public class StaffTaskServlet extends HttpServlet {

    private StaffDAO staffDAO;
    private TaskDAO taskDAO;
    private NotificationDAO notificationDAO;
    private TaskAssignmentService assignmentService;

    @Override
    public void init() throws ServletException {
        staffDAO = new StaffDAO();
        taskDAO = new TaskDAO();
        notificationDAO = new NotificationDAO();
        assignmentService = new TaskAssignmentService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        User user = (User) session.getAttribute("user");
        String role = user.getRole();

        if (!"STAFF".equals(role) && !"MANAGER".equals(role)) {
            response.sendRedirect("dashboard?error=forbidden");
            return;
        }

        Staff staff = staffDAO.getStaffByUserId(user.getId());

        List<Task> tasks;
        Map<String, Integer> stats;

        if (staff != null) {
            tasks = taskDAO.getTasksForStaff(staff.getId(), staff.getCategory());
            stats = taskDAO.getStaffTaskStats(staff.getId());
        } else {
            // Manager viewing staff view
            tasks = taskDAO.getAllTasks();
            stats = new java.util.HashMap<>();
            stats.put("assigned", 0);
            stats.put("inProgress", 0);
            stats.put("completed", 0);
            for (Task t : tasks) {
                if ("ASSIGNED".equalsIgnoreCase(t.getStatus())) stats.put("assigned", stats.get("assigned") + 1);
                else if ("IN_PROGRESS".equalsIgnoreCase(t.getStatus())) stats.put("inProgress", stats.get("inProgress") + 1);
                else if ("COMPLETED".equalsIgnoreCase(t.getStatus())) stats.put("completed", stats.get("completed") + 1);
            }
        }

        List<Notification> notifications = notificationDAO.getNotificationsByUserId(user.getId());
        int unreadCount = notificationDAO.getUnreadCount(user.getId());

        request.setAttribute("staff", staff);
        request.setAttribute("tasks", tasks);
        request.setAttribute("stats", stats);
        request.setAttribute("notifications", notifications);
        request.setAttribute("unreadCount", unreadCount);

        request.getRequestDispatcher("staff-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        User user = (User) session.getAttribute("user");
        Staff staff = staffDAO.getStaffByUserId(user.getId());

        if (staff == null && !"MANAGER".equals(user.getRole())) {
            response.sendRedirect("staff-tasks?error=staff_profile_missing");
            return;
        }

        String action = request.getParameter("action");

        // Action: Toggle Duty Status (AVAILABLE <-> OFF_DUTY)
        if ("toggle_duty".equalsIgnoreCase(action)) {
            if (staff != null) {
                String newStatus = "OFF_DUTY".equalsIgnoreCase(staff.getWorkStatus()) ? "AVAILABLE" : "OFF_DUTY";
                staffDAO.updateWorkStatus(staff.getId(), newStatus);
                response.sendRedirect("staff-tasks?msg=duty_updated");
                return;
            }
        }

        String taskIdStr = request.getParameter("taskId");
        if (taskIdStr == null || taskIdStr.trim().isEmpty()) {
            response.sendRedirect("staff-tasks?error=invalid_task");
            return;
        }

        try {
            int taskId = Integer.parseInt(taskIdStr.trim());
            int staffId = staff != null ? staff.getId() : 1;

            if ("accept".equalsIgnoreCase(action)) {
                boolean ok = taskDAO.acceptTask(taskId, staffId);
                if (ok) {
                    response.sendRedirect("staff-tasks?msg=accepted");
                } else {
                    response.sendRedirect("staff-tasks?error=accept_failed");
                }
                return;
            } else if ("decline".equalsIgnoreCase(action)) {
                // Deduct 5 performance credits for declining assigned work
                staffDAO.adjustPerformanceCredits(staffId, -5);

                // Intelligently reassign to next available department staff
                Staff newStaff = assignmentService.reassignTask(taskId, staffId);
                if (newStaff != null) {
                    response.sendRedirect("staff-tasks?msg=declined_reassigned");
                } else {
                    response.sendRedirect("staff-tasks?msg=declined_unassigned");
                }
                return;
            } else if ("complete".equalsIgnoreCase(action)) {
                boolean ok = taskDAO.completeTask(taskId, staffId);
                if (ok) {
                    response.sendRedirect("staff-tasks?msg=completed");
                } else {
                    response.sendRedirect("staff-tasks?error=complete_failed");
                }
                return;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        response.sendRedirect("staff-tasks");
    }
}
