package com.hotel.controller;

import com.hotel.dao.NotificationDAO;
import com.hotel.dao.StaffDAO;
import com.hotel.dao.TaskDAO;
import com.hotel.model.Notification;
import com.hotel.model.Staff;
import com.hotel.model.Task;
import com.hotel.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ManagerTaskServlet - Central control center for managers to monitor all operational tasks,
 * review automated assignments, resolve unassigned/pending queues, and manually intervene.
 * Mapped to /manager-tasks
 */
@WebServlet("/manager-tasks")
public class ManagerTaskServlet extends HttpServlet {

    private TaskDAO taskDAO;
    private StaffDAO staffDAO;
    private NotificationDAO notificationDAO;

    @Override
    public void init() throws ServletException {
        taskDAO = new TaskDAO();
        staffDAO = new StaffDAO();
        notificationDAO = new NotificationDAO();
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
        if (!"MANAGER".equals(user.getRole())) {
            response.sendRedirect("dashboard?error=forbidden");
            return;
        }

        String filter = request.getParameter("filter");
        if (filter == null || filter.trim().isEmpty()) {
            filter = "ALL";
        }

        List<Task> allTasks = taskDAO.getAllTasks();
        List<Task> filteredTasks = new ArrayList<>();

        int totalCount = allTasks.size();
        int pendingCount = 0;
        int inProgressCount = 0;
        int completedCount = 0;
        int urgentCount = 0;

        for (Task t : allTasks) {
            if (t.getAssignedStaffId() == null || t.getAssignedStaffId() == 0) {
                pendingCount++;
            }
            if ("IN_PROGRESS".equalsIgnoreCase(t.getStatus())) {
                inProgressCount++;
            } else if ("COMPLETED".equalsIgnoreCase(t.getStatus())) {
                completedCount++;
            }
            if ("URGENT".equalsIgnoreCase(t.getPriority()) || "HIGH".equalsIgnoreCase(t.getPriority())) {
                urgentCount++;
            }

            // Filtering
            if ("ALL".equalsIgnoreCase(filter)) {
                filteredTasks.add(t);
            } else if ("PENDING".equalsIgnoreCase(filter)) {
                if (t.getAssignedStaffId() == null || t.getAssignedStaffId() == 0) {
                    filteredTasks.add(t);
                }
            } else if ("IN_PROGRESS".equalsIgnoreCase(filter)) {
                if ("IN_PROGRESS".equalsIgnoreCase(t.getStatus())) {
                    filteredTasks.add(t);
                }
            } else if ("COMPLETED".equalsIgnoreCase(filter)) {
                if ("COMPLETED".equalsIgnoreCase(t.getStatus())) {
                    filteredTasks.add(t);
                }
            } else if ("URGENT".equalsIgnoreCase(filter)) {
                if ("URGENT".equalsIgnoreCase(t.getPriority()) || "HIGH".equalsIgnoreCase(t.getPriority())) {
                    filteredTasks.add(t);
                }
            }
        }

        List<Staff> staffList = staffDAO.getAllStaff();
        List<Notification> notifications = notificationDAO.getNotificationsByUserId(user.getId());
        int unreadNotifications = notificationDAO.getUnreadCount(user.getId());

        Map<String, Integer> metrics = new HashMap<>();
        metrics.put("total", totalCount);
        metrics.put("pending", pendingCount);
        metrics.put("inProgress", inProgressCount);
        metrics.put("completed", completedCount);
        metrics.put("urgent", urgentCount);

        request.setAttribute("tasks", filteredTasks);
        request.setAttribute("staffList", staffList);
        request.setAttribute("notifications", notifications);
        request.setAttribute("unreadCount", unreadNotifications);
        metrics.put("activeStaff", staffList.size());
        request.setAttribute("metrics", metrics);
        request.setAttribute("currentFilter", filter);

        request.getRequestDispatcher("manager-dashboard.jsp").forward(request, response);
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
        if (!"MANAGER".equals(user.getRole())) {
            response.sendRedirect("dashboard?error=forbidden");
            return;
        }

        String action = request.getParameter("action");
        String taskIdStr = request.getParameter("taskId");
        String staffIdStr = request.getParameter("staffId");

        if (taskIdStr == null || taskIdStr.trim().isEmpty()) {
            response.sendRedirect("manager-tasks?error=missing_task");
            return;
        }

        try {
            int taskId = Integer.parseInt(taskIdStr.trim());

            if ("manual_assign".equalsIgnoreCase(action)) {
                if (staffIdStr == null || staffIdStr.trim().isEmpty()) {
                    response.sendRedirect("manager-tasks?error=missing_staff");
                    return;
                }
                int staffId = Integer.parseInt(staffIdStr.trim());

                // Assign the task atomically
                boolean ok = taskDAO.assignTaskAtomic(taskId, staffId);
                if (ok) {
                    Staff targetStaff = staffDAO.getStaffById(staffId);
                    if (targetStaff != null) {
                        notificationDAO.createNotification(
                            targetStaff.getUserId(),
                            "Manual Task Assignment by Manager",
                            "Task #" + taskId + " has been manually assigned to you by Manager " + user.getUsername() + "."
                        );
                    }
                    response.sendRedirect("manager-tasks?msg=manually_assigned");
                } else {
                    response.sendRedirect("manager-tasks?error=assign_failed");
                }
                return;
            } else if ("cancel_assignment".equalsIgnoreCase(action)) {
                taskDAO.clearTaskAssignment(taskId);
                response.sendRedirect("manager-tasks?msg=assignment_cleared");
                return;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        response.sendRedirect("manager-tasks");
    }
}
