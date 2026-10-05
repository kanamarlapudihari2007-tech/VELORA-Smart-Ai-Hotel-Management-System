<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.hotel.model.Room" %>
<%@ page import="com.hotel.model.RoomType" %>
<%@ page import="com.hotel.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || !"MANAGER".equals(currentUser.getRole())) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }
    List<Room> rooms = (List<Room>) request.getAttribute("rooms");
    List<RoomType> roomTypes = (List<RoomType>) request.getAttribute("roomTypes");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Room Inventory Management — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        .inventory-layout {
            display: grid;
            grid-template-columns: 320px 1fr;
            gap: var(--space-6);
            align-items: start;
        }
        @media (max-width: 900px) {
            .inventory-layout {
                grid-template-columns: 1fr;
            }
        }
        .inline-status-form {
            display: flex;
            gap: 6px;
            align-items: center;
        }
        .inline-select {
            background: #000000;
            color: #ffffff;
            border: 1px solid var(--border-medium);
            padding: 4px 8px;
            border-radius: var(--radius-sm);
            font-size: 0.8rem;
            outline: none;
        }
        .inline-select:focus {
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
            <a href="manager-tasks" class="nav-tab-item">Tasks & Dispatch</a>
            <a href="rooms" class="nav-tab-item active">Room Inventory</a>
            <a href="my-bookings" class="nav-tab-item">Bookings Ledger</a>
            <a href="billing" class="nav-tab-item">Billing & Invoices</a>
            <a href="dynamic-pricing" class="nav-tab-item">Dynamic Pricing</a>
            <a href="analytics" class="nav-tab-item">Analytics Briefing</a>
            <a href="manager-reviews" class="nav-tab-item">Reviews & Sentiment</a>
        </nav>
    </header>

    <main class="app-container">

        <div class="page-header">
            <div>
                <h1 class="page-title">Hotel Room Inventory & Housekeeping Status</h1>
                <p class="page-subtitle">Configure hotel suites, adjust floor allocations, and audit real-time housekeeping or maintenance states.</p>
            </div>
        </div>

        <% if (errorMessage != null) { %>
            <div class="alert alert-danger">
                <span>✕</span>
                <div><%= errorMessage %></div>
            </div>
        <% } %>

        <% if ("added".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>New room added to hotel inventory successfully!</div>
            </div>
        <% } else if ("updated".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Room housekeeping & occupancy status updated successfully!</div>
            </div>
        <% } else if ("blocked".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Room blocked successfully (Status set to Maintenance). It is now hidden from customer bookings for today.</div>
            </div>
        <% } else if ("unblocked".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Room unblocked successfully! It is now live and available for customer bookings.</div>
            </div>
        <% } else if ("deleted".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Room permanently deleted from hotel inventory.</div>
            </div>
        <% } else if ("has_bookings".equals(request.getParameter("error"))) { %>
            <div class="alert alert-danger">
                <span>⚠</span>
                <div>Cannot delete this room because it has existing booking records. Use "🚫 Block" instead to safely take it off the market without corrupting guest and financial history.</div>
            </div>
        <% } else if ("delete_failed".equals(request.getParameter("error"))) { %>
            <div class="alert alert-danger">
                <span>⚠</span>
                <div>Failed to delete room due to database error.</div>
            </div>
        <% } %>

        <div class="inventory-layout">
            <!-- Add New Room Card -->
            <div class="card">
                <div class="card-header">
                    <h2 class="card-title">Add New Room</h2>
                </div>

                <form action="rooms" method="POST">
                    <input type="hidden" name="action" value="add">

                    <div class="form-group">
                        <label for="roomNumber" class="form-label">Room Number *</label>
                        <input type="text" id="roomNumber" name="roomNumber" class="form-control" placeholder="e.g. 104 or 402" required>
                    </div>

                    <div class="form-group">
                        <label for="floorNumber" class="form-label">Floor Number *</label>
                        <input type="number" id="floorNumber" name="floorNumber" class="form-control" placeholder="e.g. 1" min="1" max="15" required>
                    </div>

                    <div class="form-group">
                        <label for="roomTypeId" class="form-label">Room Category *</label>
                        <select id="roomTypeId" name="roomTypeId" class="form-control" required>
                            <% if (roomTypes != null) { 
                                for (RoomType type : roomTypes) { %>
                                    <option value="<%= type.getId() %>">
                                        <%= type.getTypeName() %> (₹<%= type.getPricePerNight() %>/night)
                                    </option>
                            <%  }
                               } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="status" class="form-label">Initial Status</label>
                        <select id="status" name="status" class="form-control">
                            <option value="AVAILABLE">AVAILABLE</option>
                            <option value="CLEANING">CLEANING</option>
                            <option value="MAINTENANCE">MAINTENANCE</option>
                        </select>
                    </div>

                    <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: var(--space-2);">
                        + Add Room to Roster
                    </button>
                </form>
            </div>

            <!-- Rooms Inventory Table -->
            <div class="card" style="padding: 0; overflow: hidden;">
                <div style="padding: var(--space-4) var(--space-6); border-bottom: 1px solid var(--border-subtle); display: flex; justify-content: space-between; align-items: center;">
                    <div style="font-size: 0.95rem; font-weight: 600; color: #ffffff;">
                        Active Room Roster (<%= rooms != null ? rooms.size() : 0 %> Rooms)
                    </div>
                </div>

                <div class="table-container">
                    <table class="table-modern">
                        <thead>
                            <tr>
                                <th>Room #</th>
                                <th>Floor</th>
                                <th>Category</th>
                                <th>Base Tariff</th>
                                <th>Current Status</th>
                                <th>Update State</th>
                                <th style="text-align: right;">Quick Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (rooms != null && !rooms.isEmpty()) {
                                for (Room room : rooms) { 
                                    String badgeClass = "badge-success";
                                    if ("RESERVED".equals(room.getStatus())) badgeClass = "badge-neutral";
                                    else if ("OCCUPIED".equals(room.getStatus())) badgeClass = "badge-warning";
                                    else if ("CLEANING".equals(room.getStatus())) badgeClass = "badge-neutral";
                                    else if ("MAINTENANCE".equals(room.getStatus())) badgeClass = "badge-danger";
                            %>
                                <tr>
                                    <td><strong style="color: #ffffff;">Room <%= room.getRoomNumber() %></strong></td>
                                    <td>Floor <%= room.getFloorNumber() %></td>
                                    <td><strong style="color: #ededed;"><%= room.getTypeName() %></strong></td>
                                    <td class="mono" style="font-weight: 600;">₹<%= room.getPricePerNight() %></td>
                                    <td>
                                        <span class="badge <%= badgeClass %>">
                                            <%= "MAINTENANCE".equals(room.getStatus()) ? "🚫 BLOCKED" : room.getStatus() %>
                                        </span>
                                    </td>
                                    <td>
                                        <form action="rooms" method="POST" class="inline-status-form">
                                            <input type="hidden" name="action" value="updateStatus">
                                            <input type="hidden" name="roomId" value="<%= room.getId() %>">
                                            <select name="status" class="inline-select">
                                                <option value="AVAILABLE" <%= "AVAILABLE".equals(room.getStatus()) ? "selected" : "" %>>AVAILABLE</option>
                                                <option value="RESERVED" <%= "RESERVED".equals(room.getStatus()) ? "selected" : "" %>>RESERVED</option>
                                                <option value="OCCUPIED" <%= "OCCUPIED".equals(room.getStatus()) ? "selected" : "" %>>OCCUPIED</option>
                                                <option value="CLEANING" <%= "CLEANING".equals(room.getStatus()) ? "selected" : "" %>>CLEANING</option>
                                                <option value="MAINTENANCE" <%= "MAINTENANCE".equals(room.getStatus()) ? "selected" : "" %>>MAINTENANCE</option>
                                            </select>
                                            <button type="submit" class="btn btn-secondary btn-sm">Save</button>
                                        </form>
                                    </td>
                                    <td style="text-align: right; white-space: nowrap;">
                                        <% if ("MAINTENANCE".equals(room.getStatus())) { %>
                                            <form action="rooms" method="POST" style="display:inline; margin:0;">
                                                <input type="hidden" name="action" value="unblockRoom">
                                                <input type="hidden" name="roomId" value="<%= room.getId() %>">
                                                <button type="submit" class="btn btn-primary btn-sm" title="Re-open room for customer bookings">
                                                    ✓ Unblock
                                                </button>
                                            </form>
                                        <% } else { %>
                                            <form action="rooms" method="POST" style="display:inline; margin:0;">
                                                <input type="hidden" name="action" value="blockRoom">
                                                <input type="hidden" name="roomId" value="<%= room.getId() %>">
                                                <button type="submit" class="btn btn-secondary btn-sm" style="color: #f87171; border-color: rgba(248,113,113,0.3);" onclick="return confirm('Block Room <%= room.getRoomNumber() %> from guest bookings for today?');" title="Take room off the market for today">
                                                    🚫 Block
                                                </button>
                                            </form>
                                        <% } %>

                                        <form action="rooms" method="POST" style="display:inline; margin:0; margin-left: 4px;">
                                            <input type="hidden" name="action" value="deleteRoom">
                                            <input type="hidden" name="roomId" value="<%= room.getId() %>">
                                            <button type="submit" class="btn btn-secondary btn-sm" style="color: #94a3b8; padding: 4px 8px;" onclick="return confirm('Delete Room <%= room.getRoomNumber() %> permanently? (Note: Rooms with existing booking records are protected and cannot be deleted)');" title="Permanently delete room">
                                                🗑️
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            <%  }
                               } else { %>
                                <tr>
                                    <td colspan="6" style="text-align: center; color: var(--text-muted); padding: var(--space-8);">No rooms found in database.</td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    </main>

</body>
</html>
