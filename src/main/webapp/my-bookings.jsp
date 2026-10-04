<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="com.hotel.model.Booking" %>
<%@ page import="com.hotel.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }

    List<Booking> bookings = (List<Booking>) request.getAttribute("bookings");
    Set<Integer> reviewedBookingIds = (Set<Integer>) request.getAttribute("reviewedBookingIds");
    String msg = request.getParameter("msg");
    String code = request.getParameter("code");
    boolean isManager = "MANAGER".equals(currentUser.getRole());
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= isManager ? "Reservations Master Ledger" : "My Stays & Bookings" %> — Velora</title>
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
                <a href="<%= isManager ? "manager-tasks" : "dashboard" %>" class="brand-badge">
                    <img src="images/velora-mark.png" alt="Velora" class="brand-logo-img">
                    <span>Velora</span>
                </a>
                <span class="brand-separator">/</span>
                <span class="brand-project-switcher"><%= isManager ? "Master Ledger" : "Guest Portal" %></span>
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
            <% if (isManager) { %>
                <a href="manager-tasks" class="nav-tab-item">Tasks & Dispatch</a>
                <a href="rooms" class="nav-tab-item">Room Inventory</a>
                <a href="my-bookings" class="nav-tab-item active">Bookings Ledger</a>
                <a href="billing" class="nav-tab-item">Billing & Invoices</a>
                <a href="dynamic-pricing" class="nav-tab-item">Dynamic Pricing</a>
                <a href="analytics" class="nav-tab-item">Analytics Briefing</a>
                <a href="manager-reviews" class="nav-tab-item">Reviews & Sentiment</a>
            <% } else { %>
                <a href="dashboard" class="nav-tab-item">Overview</a>
                <a href="search-rooms" class="nav-tab-item">Search & Book</a>
                <a href="my-bookings" class="nav-tab-item active">My Stays</a>
                <a href="service-request" class="nav-tab-item">Room Service</a>
                <a href="complaint" class="nav-tab-item">Report Issue</a>
                <a href="assistant" class="nav-tab-item">AI Concierge</a>
                <a href="billing" class="nav-tab-item">Billing & Invoices</a>
                <a href="review" class="nav-tab-item">Reviews</a>
            <% } %>
        </nav>
    </header>

    <main class="app-container">

        <div class="page-header">
            <div>
                <h1 class="page-title"><%= isManager ? "Hotel Reservation Records & History" : "My Hotel Stays & Reservations" %></h1>
                <p class="page-subtitle">Track your booking codes, room allocations, active check-in status, and certified stay invoices.</p>
            </div>
            <% if (!isManager) { %>
                <a href="search-rooms" class="btn btn-primary btn-sm">+ Book Another Room</a>
            <% } %>
        </div>

        <% if ("booked".equals(msg) && code != null) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>
                    <strong>Reservation Confirmed!</strong> Your booking code is <span class="mono" style="font-weight: 700; color: #ffffff;"><%= code %></span>. Room status has been secured as RESERVED.
                </div>
            </div>
        <% } else if ("checked_in".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div><strong>Check-in Successful!</strong> Room status updated to OCCUPIED. Welcome to Velora Hotel!</div>
            </div>
        <% } else if ("checked_out".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div><strong>Check-out Completed!</strong> Room released to Housekeeping queue. Thank you for your stay!</div>
            </div>
        <% } %>

        <div class="card" style="padding: 0; overflow: hidden;">
            <div class="table-container">
                <table class="table-modern">
                    <thead>
                        <tr>
                            <th>Booking Ref</th>
                            <% if (isManager) { %>
                                <th>Guest Name</th>
                            <% } %>
                            <th>Room</th>
                            <th>Category</th>
                            <th>Check-In</th>
                            <th>Check-Out</th>
                            <th>Total Tariff</th>
                            <th>Stay Status</th>
                            <th style="text-align: right;">Action & Invoice</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (bookings != null && !bookings.isEmpty()) {
                            for (Booking b : bookings) { 
                                String statusBadge = "badge-neutral";
                                if ("CHECKED_IN".equals(b.getStatus())) statusBadge = "badge-success";
                                else if ("RESERVED".equals(b.getStatus())) statusBadge = "badge-neutral";
                                else if ("CANCELLED".equals(b.getStatus())) statusBadge = "badge-danger";
                        %>
                            <tr>
                                <td><span class="mono" style="font-weight: 600; color: #ffffff;"><%= b.getBookingCode() %></span></td>
                                <% if (isManager) { %>
                                    <td><strong style="color: #ededed;"><%= b.getGuestName() != null ? b.getGuestName() : "Guest #" + b.getGuestId() %></strong></td>
                                <% } %>
                                <td><strong>Room <%= b.getRoomNumber() %></strong></td>
                                <td><%= b.getTypeName() %></td>
                                <td class="mono" style="font-size: 0.8rem; color: var(--text-secondary);"><%= b.getCheckInDate() %></td>
                                <td class="mono" style="font-size: 0.8rem; color: var(--text-secondary);"><%= b.getCheckOutDate() %></td>
                                <td><strong class="mono" style="color: #ffffff;">₹<%= b.getTotalAmount() %></strong></td>
                                <td><span class="badge <%= statusBadge %>"><%= b.getStatus() %></span></td>
                                <td style="text-align: right; white-space: nowrap;">
                                    <% if ("RESERVED".equals(b.getStatus())) { %>
                                        <a href="checkin?bookingId=<%= b.getId() %>" class="btn btn-primary btn-sm">Check-In</a>
                                        <a href="billing?bookingId=<%= b.getId() %>" class="btn btn-secondary btn-sm" style="margin-left: 4px;">Receipt</a>
                                    <% } else if ("CHECKED_IN".equals(b.getStatus())) { %>
                                        <a href="billing?bookingId=<%= b.getId() %>" class="btn btn-primary btn-sm">Check-Out & Bill</a>
                                    <% } else if ("CHECKED_OUT".equals(b.getStatus())) { %>
                                        <a href="billing?bookingId=<%= b.getId() %>" class="btn btn-secondary btn-sm">Invoice</a>
                                        <% boolean isReviewed = (reviewedBookingIds != null && reviewedBookingIds.contains(b.getId())); %>
                                        <% if (isReviewed) { %>
                                            <a href="review?bookingId=<%= b.getId() %>" class="btn btn-secondary btn-sm" style="margin-left: 4px; color: var(--success);">✓ Reviewed</a>
                                        <% } else { %>
                                            <a href="review?bookingId=<%= b.getId() %>" class="btn btn-primary btn-sm" style="margin-left: 4px;">Review →</a>
                                        <% } %>
                                    <% } else { %>
                                        <span style="color: var(--text-muted); font-size: 0.8rem;">Cancelled</span>
                                    <% } %>
                                </td>
                            </tr>
                        <%  }
                           } else { %>
                            <tr>
                                <td colspan="<%= isManager ? 9 : 8 %>" style="text-align: center; color: var(--text-muted); padding: var(--space-8);">
                                    You have no active reservations yet. <a href="search-rooms" style="color: #ffffff; text-decoration: underline;">Search & Book a Room</a>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>

    </main>

</body>
</html>
