<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hotel.model.User" %>
<%@ page import="com.hotel.model.Staff" %>
<%@ page import="com.hotel.model.Task" %>
<%@ page import="com.hotel.model.Notification" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || !"MANAGER".equals(currentUser.getRole())) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }

    List<Task> tasks = (List<Task>) request.getAttribute("tasks");
    List<Staff> staffList = (List<Staff>) request.getAttribute("staffList");
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
    Integer unreadCount = (Integer) request.getAttribute("unreadCount");
    Map<String, Integer> metrics = (Map<String, Integer>) request.getAttribute("metrics");
    String currentFilter = (String) request.getAttribute("currentFilter");
    if (currentFilter == null) currentFilter = "ALL";

    int totalTasks = (metrics != null && metrics.containsKey("total")) ? metrics.get("total") : 0;
    int pendingTasks = (metrics != null && metrics.containsKey("pending")) ? metrics.get("pending") : 0;
    int inProgressTasks = (metrics != null && metrics.containsKey("inProgress")) ? metrics.get("inProgress") : 0;
    int completedTasks = (metrics != null && metrics.containsKey("completed")) ? metrics.get("completed") : 0;
    int urgentTasks = (metrics != null && metrics.containsKey("urgent")) ? metrics.get("urgent") : 0;
    int activeStaff = (metrics != null && metrics.containsKey("activeStaff")) ? metrics.get("activeStaff") : 0;

    String msg = request.getParameter("msg");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tasks & Operations Dispatch — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        .assign-inline-form {
            display: flex;
            gap: 6px;
            align-items: center;
        }
        .assign-select {
            background: var(--bg-input);
            color: var(--text-primary);
            border: 1px solid var(--border-medium);
            padding: 4px 8px;
            border-radius: var(--radius-sm);
            font-size: 0.8rem;
            max-width: 170px;
            outline: none;
        }
        .assign-select:focus {
            border-color: #ffffff;
        }
    </style>
</head>
<body>

    <!-- Vercel Dual-Rail Header -->
    <header class="app-navbar">
        <div class="navbar-top">
            <div style="display: flex; align-items: center; gap: var(--space-3);">
                <a href="manager-tasks" class="brand-badge">
                    <img src="images/velora-mark.png" alt="Velora" class="brand-logo-img">
                    <span>Velora</span>
                </a>
                <span class="brand-separator">/</span>
                <span class="brand-project-switcher">Management Console</span>
                <span class="brand-env-badge">Production</span>
            </div>
            <div class="nav-actions">
                <div class="user-profile-chip">
                    <span class="user-status-dot"></span>
                    <span><%= currentUser.getUsername() %></span>
                </div>
                <a href="logout" class="btn btn-secondary btn-sm">Logout</a>
            </div>
        </div>

        <nav class="navbar-tabs">
            <a href="manager-tasks" class="nav-tab-item active">Tasks & Dispatch</a>
            <a href="rooms" class="nav-tab-item">Room Inventory</a>
            <a href="my-bookings" class="nav-tab-item">Bookings Ledger</a>
            <a href="billing" class="nav-tab-item">Billing & Invoices</a>
            <a href="dynamic-pricing" class="nav-tab-item">Dynamic Pricing</a>
            <a href="analytics" class="nav-tab-item">Analytics Briefing</a>
            <a href="manager-reviews" class="nav-tab-item">Reviews & Sentiment</a>
        </nav>
    </header>

    <main class="app-container">

        <!-- Page Header -->
        <div class="page-header">
            <div>
                <h1 class="page-title">Tasks & Intelligent Dispatch</h1>
                <p class="page-subtitle">Real-time telemetry of hotel operational workloads, automated task allocation, and department queues.</p>
            </div>
            <div style="display: flex; gap: var(--space-2); flex-wrap: wrap;">
                <a href="dynamic-pricing" class="btn btn-secondary btn-sm">Dynamic Pricing</a>
                <a href="analytics" class="btn btn-primary btn-sm">Executive Analytics</a>
            </div>
        </div>

        <% if ("manually_assigned".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Task manually assigned to staff member successfully. Notification dispatched.</div>
            </div>
        <% } else if ("assignment_cleared".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Task assignment cleared. Task returned to pending dispatch queue.</div>
            </div>
        <% } else if (error != null) { %>
            <div class="alert alert-danger">
                <span>⚠️</span>
                <div>Action failed: <%= error %></div>
            </div>
        <% } %>

        <!-- Monochromatic KPI Metrics Grid -->
        <div class="metrics-grid">
            <div class="metric-card">
                <div class="metric-label">Total Hotel Tasks</div>
                <div class="metric-value"><%= totalTasks %></div>
                <div class="metric-sub">Across all departments</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">Pending / Unassigned</div>
                <div class="metric-value <%= pendingTasks > 0 ? "warning" : "" %>"><%= pendingTasks %></div>
                <div class="metric-sub">Requires staff assignment</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">In Progress</div>
                <div class="metric-value"><%= inProgressTasks %></div>
                <div class="metric-sub">Currently under execution</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">Completed Tasks</div>
                <div class="metric-value"><%= completedTasks %></div>
                <div class="metric-sub">Resolved and verified</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">Urgent / High Priority</div>
                <div class="metric-value <%= urgentTasks > 0 ? "danger" : "" %>"><%= urgentTasks %></div>
                <div class="metric-sub">SLA critical tickets</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">Registered Staff</div>
                <div class="metric-value"><%= activeStaff %></div>
                <div class="metric-sub">On-duty workforce</div>
            </div>
        </div>

        <!-- Real-Time Alerts Drawer -->
        <% if (notifications != null && !notifications.isEmpty()) { %>
            <div class="card" style="margin-bottom: var(--space-6); background: var(--bg-surface); border-color: var(--border-medium);">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-3);">
                    <div style="font-size: 0.85rem; font-weight: 600; color: #ffffff;">
                        Operational Alerts & Automated Assignment Events
                    </div>
                    <% if (unreadCount != null && unreadCount > 0) { %>
                        <span class="badge badge-danger"><%= unreadCount %> Unread</span>
                    <% } %>
                </div>
                <div style="display: flex; flex-direction: column; gap: var(--space-2);">
                    <% for (int i = 0; i < Math.min(3, notifications.size()); i++) { 
                        Notification n = notifications.get(i);
                    %>
                        <div style="padding: var(--space-2) 0; border-bottom: 1px solid var(--border-subtle); font-size: 0.85rem; color: var(--text-secondary);">
                            <strong style="color: #ffffff;"><%= n.getTitle() %>:</strong> <%= n.getMessage() %>
                            <div class="mono" style="font-size: 0.72rem; color: var(--text-muted); margin-top: 2px;"><%= n.getCreatedAt() %></div>
                        </div>
                    <% } %>
                </div>
            </div>
        <% } %>

        <!-- Operational Tasks & Dispatch Table -->
        <div class="card" style="margin-bottom: var(--space-8); padding: 0; overflow: hidden;">
            <div style="padding: var(--space-5) var(--space-6); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--space-3); border-bottom: 1px solid var(--border-subtle);">
                <div>
                    <h2 class="card-title">Operational Tasks</h2>
                    <p style="font-size: 0.82rem; color: var(--text-muted); margin-top: 2px;">
                        Queue of active guest requests, room cleaning duties, and maintenance tickets.
                    </p>
                </div>
                <div style="display: flex; gap: 6px; flex-wrap: wrap;">
                    <a href="manager-tasks?filter=ALL" class="filter-chip <%= "ALL".equals(currentFilter) ? "active" : "" %>">All (<%= totalTasks %>)</a>
                    <a href="manager-tasks?filter=PENDING" class="filter-chip <%= "PENDING".equals(currentFilter) ? "active" : "" %>">Pending (<%= pendingTasks %>)</a>
                    <a href="manager-tasks?filter=IN_PROGRESS" class="filter-chip <%= "IN_PROGRESS".equals(currentFilter) ? "active" : "" %>">In Progress (<%= inProgressTasks %>)</a>
                    <a href="manager-tasks?filter=COMPLETED" class="filter-chip <%= "COMPLETED".equals(currentFilter) ? "active" : "" %>">Completed (<%= completedTasks %>)</a>
                    <a href="manager-tasks?filter=URGENT" class="filter-chip <%= "URGENT".equals(currentFilter) ? "active" : "" %>">Urgent (<%= urgentTasks %>)</a>
                </div>
            </div>

            <div class="table-container" style="border: none; border-radius: 0;">
                <table class="table-modern">
                    <thead>
                        <tr>
                            <th>Task Ref</th>
                            <th>Category</th>
                            <th>Room</th>
                            <th>Description</th>
                            <th>Priority</th>
                            <th>Assigned Staff</th>
                            <th>Status</th>
                            <th>Created Time</th>
                            <th>Intervention</th>
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
                                <td style="max-width: 240px; line-height: 1.4; color: var(--text-secondary); font-size: 0.82rem;"><%= t.getNotes() != null ? t.getNotes() : "-" %></td>
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
                                    <% if (t.getAssignedStaffId() != null && t.getAssignedStaffId() > 0) { %>
                                        <div style="font-weight: 600; color: #ffffff;"><%= t.getStaffName() %></div>
                                        <div style="font-size: 0.72rem; color: var(--text-muted);"><%= t.getStaffCategory() %></div>
                                    <% } else { %>
                                        <span class="badge badge-danger">Unassigned</span>
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
                                <td class="mono" style="font-size: 0.75rem; color: var(--text-muted);"><%= t.getCreatedAt() %></td>
                                <td>
                                    <% if (!"COMPLETED".equalsIgnoreCase(t.getStatus())) { %>
                                        <form method="post" action="manager-tasks" class="assign-inline-form">
                                            <input type="hidden" name="action" value="manual_assign">
                                            <input type="hidden" name="taskId" value="<%= t.getId() %>">
                                            <select name="staffId" class="assign-select" required>
                                                <option value="">-- Assign Staff --</option>
                                                <% if (staffList != null) {
                                                    for (Staff s : staffList) { %>
                                                        <option value="<%= s.getId() %>" <%= (t.getAssignedStaffId() != null && t.getAssignedStaffId() == s.getId()) ? "selected" : "" %>>
                                                            <%= s.getFullName() %> (<%= s.getCategory() %> - <%= s.getWorkStatus() %>)
                                                        </option>
                                                <%   }
                                                } %>
                                            </select>
                                            <button type="submit" class="btn btn-secondary btn-sm">Assign</button>
                                        </form>
                                    <% } else { %>
                                        <span style="color: var(--success-text); font-size: 0.8rem; font-weight: 500;">✓ Resolved</span>
                                    <% } %>
                                </td>
                            </tr>
                        <%
                                }
                            } else {
                        %>
                            <tr>
                                <td colspan="9" style="text-align: center; padding: var(--space-8); color: var(--text-muted);">
                                    No tasks match filter '<%= currentFilter %>'.
                                </td>
                            </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Staff Workload Roster -->
        <div class="card" style="padding: 0; overflow: hidden; margin-bottom: var(--space-8);">
            <div style="padding: var(--space-5) var(--space-6); border-bottom: 1px solid var(--border-subtle);">
                <h2 class="card-title">Staff Workload & Performance Credits</h2>
                <p style="font-size: 0.82rem; color: var(--text-muted); margin-top: 2px;">
                    Workforce utilization and performance reward auditing across all hotel departments.
                </p>
            </div>

            <div class="table-container" style="border: none; border-radius: 0;">
                <table class="table-modern">
                    <thead>
                        <tr>
                            <th>Staff Member</th>
                            <th>Department</th>
                            <th>Contact Phone</th>
                            <th>Duty Status</th>
                            <th>Active Workload</th>
                            <th>Performance Credits</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (staffList != null && !staffList.isEmpty()) {
                            for (Staff s : staffList) { %>
                                <tr>
                                    <td>
                                        <div style="font-weight: 600; color: #ffffff;"><%= s.getFullName() %></div>
                                        <div class="mono" style="font-size: 0.72rem; color: var(--text-muted);">@<%= s.getUsername() %></div>
                                    </td>
                                    <td><span class="badge badge-neutral"><%= s.getCategory() %></span></td>
                                    <td class="mono" style="font-size: 0.82rem; color: var(--text-secondary);"><%= s.getPhone() != null ? s.getPhone() : "N/A" %></td>
                                    <td>
                                        <% if ("AVAILABLE".equalsIgnoreCase(s.getWorkStatus())) { %>
                                            <span class="badge badge-success">● AVAILABLE</span>
                                        <% } else if ("BUSY".equalsIgnoreCase(s.getWorkStatus())) { %>
                                            <span class="badge badge-warning">● BUSY</span>
                                        <% } else { %>
                                            <span class="badge badge-neutral">○ OFF DUTY</span>
                                        <% } %>
                                    </td>
                                    <td><strong style="color: #ffffff;"><%= s.getActiveTaskCount() %></strong> <span style="font-size: 0.78rem; color: var(--text-muted);">active tasks</span></td>
                                    <td><span class="mono" style="font-weight: 600; color: var(--warning-text);"><%= s.getPerformanceCredits() %> pts</span></td>
                                </tr>
                        <%  }
                        } else { %>
                            <tr><td colspan="6" style="text-align: center; padding: var(--space-6); color: var(--text-muted);">No staff accounts registered.</td></tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>

    </main>

</body>
</html>
