<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hotel.model.User" %>
<%@ page import="com.hotel.model.Staff" %>
<%@ page import="com.hotel.model.Task" %>
<%@ page import="com.hotel.model.Notification" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || (!"STAFF".equals(currentUser.getRole()) && !"MANAGER".equals(currentUser.getRole()))) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }

    Staff staff = (Staff) request.getAttribute("staff");
    List<Task> tasks = (List<Task>) request.getAttribute("tasks");
    Map<String, Integer> stats = (Map<String, Integer>) request.getAttribute("stats");
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
    Integer unreadCount = (Integer) request.getAttribute("unreadCount");

    int assignedCount = (stats != null && stats.containsKey("assigned")) ? stats.get("assigned") : 0;
    int inProgressCount = (stats != null && stats.containsKey("inProgress")) ? stats.get("inProgress") : 0;
    int completedCount = (stats != null && stats.containsKey("completed")) ? stats.get("completed") : 0;

    String msg = request.getParameter("msg");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Staff Operations Portal — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
</head>
<body>

    <!-- Vercel Dual-Rail Header -->
    <header class="app-navbar">
        <div class="navbar-top">
            <div style="display: flex; align-items: center; gap: var(--space-3);">
                <a href="dashboard" class="brand-badge">
                    <img src="images/velora-mark.png" alt="Velora" class="brand-logo-img">
                    <span>Velora</span>
                </a>
                <span class="brand-separator">/</span>
                <span class="brand-project-switcher">Staff Workstation</span>
                <span class="brand-env-badge">Production</span>
            </div>
            <div class="nav-actions">
                <div class="user-profile-chip">
                    <span class="user-status-dot"></span>
                    <span><%= currentUser.getUsername() %> (<%= staff != null ? staff.getCategory() : "Staff" %>)</span>
                </div>
                <a href="logout" class="btn btn-secondary btn-sm">Logout</a>
            </div>
        </div>

        <nav class="navbar-tabs">
            <a href="dashboard" class="nav-tab-item active">Work Queue</a>
        </nav>
    </header>

    <main class="app-container">

        <!-- Staff Profile Card -->
        <div class="card" style="margin-bottom: var(--space-6); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--space-4);">
            <div>
                <div style="display: flex; align-items: center; gap: var(--space-3); flex-wrap: wrap;">
                    <h1 style="font-size: 1.35rem; font-weight: 700; color: #ffffff;"><%= staff != null ? staff.getFullName() : currentUser.getUsername() %></h1>
                    <span class="badge badge-warning mono">⭐ <%= staff != null ? staff.getPerformanceCredits() : 100 %> Credits</span>
                </div>
                <div style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 4px;">
                    Department: <strong style="color: #ffffff;"><%= staff != null ? staff.getCategory() : "General Operations" %></strong>
                    • Phone: <span class="mono"><%= staff != null ? staff.getPhone() : "N/A" %></span>
                </div>
            </div>

            <div style="display: flex; align-items: center; gap: var(--space-3);">
                <% if (staff != null && "BUSY".equalsIgnoreCase(staff.getWorkStatus())) { %>
                    <span class="badge badge-warning">● ACTIVE / BUSY</span>
                <% } else if (staff != null && "OFF_DUTY".equalsIgnoreCase(staff.getWorkStatus())) { %>
                    <span class="badge badge-neutral">○ OFF DUTY</span>
                <% } else { %>
                    <span class="badge badge-success">● AVAILABLE ON DUTY</span>
                <% } %>

                <form method="post" action="staff-tasks" style="display:inline;">
                    <input type="hidden" name="action" value="toggle_duty">
                    <input type="hidden" name="taskId" value="0">
                    <button type="submit" class="btn btn-secondary btn-sm">
                        <%= (staff != null && "OFF_DUTY".equalsIgnoreCase(staff.getWorkStatus())) ? "Go On-Duty" : "Go Off-Duty" %>
                    </button>
                </form>
            </div>
        </div>

        <% if ("accepted".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Task accepted successfully! Work status marked as <strong>IN_PROGRESS</strong>.</div>
            </div>
        <% } else if ("declined_reassigned".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Task declined and automatically reassigned to another available departmental staff member. (-5 credits applied).</div>
            </div>
        <% } else if ("declined_unassigned".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Task declined. No other departmental staff currently available; management notified.</div>
            </div>
        <% } else if ("completed".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Task completed! Performance credits awarded and room status updated in real-time.</div>
            </div>
        <% } else if ("duty_updated".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Work duty status updated successfully.</div>
            </div>
        <% } else if (error != null) { %>
            <div class="alert alert-danger">
                <span>⚠️</span>
                <div>Action failed: <%= error %></div>
            </div>
        <% } %>

        <!-- Real-Time Alerts Drawer -->
        <% if (notifications != null && !notifications.isEmpty()) { %>
            <div class="card" style="margin-bottom: var(--space-6); background: var(--bg-surface); border-color: var(--border-medium);">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-3);">
                    <div style="font-size: 0.85rem; font-weight: 600; color: #ffffff;">
                        Recent Automated Task Notifications
                    </div>
                    <% if (unreadCount != null && unreadCount > 0) { %>
                        <span class="badge badge-info"><%= unreadCount %> New</span>
                    <% } %>
                </div>
                <div style="display: flex; flex-direction: column; gap: var(--space-2);">
                    <% for (int i = 0; i < Math.min(3, notifications.size()); i++) { 
                        Notification n = notifications.get(i);
                    %>
                        <div style="padding: var(--space-2) 0; border-bottom: 1px solid var(--border-subtle); font-size: 0.85rem;">
                            <strong style="color: #ffffff;"><%= n.getTitle() %>:</strong> <%= n.getMessage() %>
                            <div class="mono" style="font-size: 0.72rem; color: var(--text-muted); margin-top: 2px;"><%= n.getCreatedAt() %></div>
                        </div>
                    <% } %>
                </div>
            </div>
        <% } %>

        <!-- Task Counters -->
        <div class="metrics-grid" style="grid-template-columns: repeat(3, 1fr); margin-bottom: var(--space-6);">
            <div class="metric-card">
                <div class="metric-label">Pending Assigned Tasks</div>
                <div class="metric-value"><%= assignedCount %></div>
            </div>
            <div class="metric-card">
                <div class="metric-label">Tasks In Progress</div>
                <div class="metric-value warning"><%= inProgressCount %></div>
            </div>
            <div class="metric-card">
                <div class="metric-label">Tasks Completed</div>
                <div class="metric-value success"><%= completedCount %></div>
            </div>
        </div>

        <!-- Work Queue Table -->
        <div class="card" style="padding: 0; overflow: hidden;">
            <div style="padding: var(--space-5) var(--space-6); display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-subtle);">
                <div>
                    <h2 class="card-title">Assigned Work Queue</h2>
                    <p style="font-size: 0.82rem; color: var(--text-muted); margin-top: 2px;">
                        Duties prioritized by urgency and AI load balancing algorithm.
                    </p>
                </div>
                <span class="badge badge-success">● Live Load Balanced</span>
            </div>

            <div class="table-container" style="border: none; border-radius: 0;">
                <table class="table-modern">
                    <thead>
                        <tr>
                            <th>Task Ref</th>
                            <th>Type</th>
                            <th>Room</th>
                            <th>Instructions</th>
                            <th>Priority</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            if (tasks != null && !tasks.isEmpty()) {
                                for (Task t : tasks) {
                        %>
                            <tr>
                                <td><span class="mono" style="font-weight: 600; color: #ffffff;">#TSK-<%= t.getId() %></span></td>
                                <td>
                                    <% if ("ROOM_CLEANING".equalsIgnoreCase(t.getTaskType())) { %>
                                        <span class="badge badge-neutral">Housekeeping</span>
                                    <% } else if ("SERVICE_REQUEST".equalsIgnoreCase(t.getTaskType())) { %>
                                        <span class="badge badge-info">Room Service</span>
                                    <% } else { %>
                                        <span class="badge badge-warning">Maintenance</span>
                                    <% } %>
                                </td>
                                <td><strong><%= t.getRoomNumber() != null ? "Room " + t.getRoomNumber() : "General" %></strong></td>
                                <td style="max-width: 280px; line-height: 1.4; color: var(--text-secondary); font-size: 0.82rem;">
                                    <%= t.getNotes() != null ? t.getNotes() : "No details provided" %>
                                </td>
                                <td>
                                    <% if ("URGENT".equalsIgnoreCase(t.getPriority())) { %>
                                        <span class="badge badge-danger">URGENT</span>
                                    <% } else if ("HIGH".equalsIgnoreCase(t.getPriority())) { %>
                                        <span class="badge badge-warning">HIGH</span>
                                    <% } else if ("LOW".equalsIgnoreCase(t.getPriority())) { %>
                                        <span class="badge badge-neutral">LOW</span>
                                    <% } else { %>
                                        <span class="badge badge-neutral">NORMAL</span>
                                    <% } %>
                                </td>
                                <td>
                                    <% if ("IN_PROGRESS".equalsIgnoreCase(t.getStatus())) { %>
                                        <span class="badge badge-warning">IN PROGRESS</span>
                                    <% } else if ("COMPLETED".equalsIgnoreCase(t.getStatus())) { %>
                                        <span class="badge badge-success">COMPLETED</span>
                                    <% } else { %>
                                        <span class="badge badge-neutral">ASSIGNED</span>
                                    <% } %>
                                </td>
                                <td>
                                    <div style="display: flex; gap: 6px; align-items: center;">
                                        <% if ("ASSIGNED".equalsIgnoreCase(t.getStatus())) { %>
                                            <form method="post" action="staff-tasks" style="display:inline;">
                                                <input type="hidden" name="action" value="accept">
                                                <input type="hidden" name="taskId" value="<%= t.getId() %>">
                                                <button type="submit" class="btn btn-primary btn-sm">Accept</button>
                                            </form>
                                            <form method="post" action="staff-tasks" style="display:inline;" onsubmit="return confirm('Decline this task? It will be automatically reassigned to another available staff member.');">
                                                <input type="hidden" name="action" value="decline">
                                                <input type="hidden" name="taskId" value="<%= t.getId() %>">
                                                <button type="submit" class="btn btn-danger btn-sm">Decline</button>
                                            </form>
                                        <% } else if ("IN_PROGRESS".equalsIgnoreCase(t.getStatus())) { %>
                                            <form method="post" action="staff-tasks" style="display:inline;">
                                                <input type="hidden" name="action" value="complete">
                                                <input type="hidden" name="taskId" value="<%= t.getId() %>">
                                                <button type="submit" class="btn btn-success btn-sm" onclick="return confirm('Mark this task as completed? Room or service status will be updated.');">Complete</button>
                                            </form>
                                        <% } else { %>
                                            <span style="color: var(--success-text); font-weight: 600; font-size: 0.8rem;">✓ Done</span>
                                        <% } %>
                                    </div>
                                </td>
                            </tr>
                        <%
                                }
                            } else {
                        %>
                            <tr>
                                <td colspan="7" style="text-align: center; padding: var(--space-8); color: var(--text-muted);">
                                    No tasks currently in your queue. All department requests are up to date.
                                </td>
                            </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>
        </div>

    </main>

</body>
</html>
