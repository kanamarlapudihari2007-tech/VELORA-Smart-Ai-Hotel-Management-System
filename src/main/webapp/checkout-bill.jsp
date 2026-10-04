<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hotel.model.Booking" %>
<%@ page import="com.hotel.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }

    Booking booking = (Booking) request.getAttribute("booking");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Final Bill & Checkout — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        .bill-summary-box {
            background: #000000;
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-sm);
            padding: var(--space-5);
            margin-bottom: var(--space-6);
        }
        .bill-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: var(--space-2) 0;
            border-bottom: 1px solid var(--border-subtle);
            font-size: 0.85rem;
            color: var(--text-secondary);
        }
        .bill-row:last-child {
            border-bottom: none;
        }
        .bill-row strong {
            color: #ededed;
        }
        .bill-total-highlight {
            background: var(--bg-surface);
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-sm);
            padding: var(--space-3) var(--space-4);
            margin-top: var(--space-4);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
    </style>
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
                <span class="brand-project-switcher">Guest Settlement</span>
                <span class="brand-env-badge">Production</span>
            </div>
            <div class="nav-actions">
                <a href="my-bookings" class="btn btn-secondary btn-sm">← Back to My Stays</a>
            </div>
        </div>

        <nav class="navbar-tabs">
            <a href="dashboard" class="nav-tab-item">Overview</a>
            <a href="search-rooms" class="nav-tab-item">Search & Book</a>
            <a href="my-bookings" class="nav-tab-item">My Stays</a>
            <a href="service-request" class="nav-tab-item">Room Service</a>
            <a href="complaint" class="nav-tab-item">Report Issue</a>
            <a href="assistant" class="nav-tab-item">AI Concierge</a>
            <a href="billing" class="nav-tab-item active">Billing & Invoices</a>
            <a href="review" class="nav-tab-item">Reviews</a>
        </nav>
    </header>

    <main class="app-container-sm">
        <div class="card">
            <div style="margin-bottom: var(--space-6);">
                <h1 style="font-size: 1.25rem; font-weight: 600; color: #ffffff; letter-spacing: -0.02em;">Final Stay Receipt & Release</h1>
                <p style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 4px;">
                    Review your completed folio before initiating room release and housekeeping handover.
                </p>
            </div>

            <% if (booking != null) { %>
                <div class="bill-summary-box">
                    <div class="bill-row">
                        <span>Booking Reference:</span>
                        <strong class="mono" style="color: #ffffff;"><%= booking.getBookingCode() %></strong>
                    </div>
                    <div class="bill-row">
                        <span>Guest Name:</span>
                        <strong><%= booking.getGuestName() != null ? booking.getGuestName() : currentUser.getUsername() %></strong>
                    </div>
                    <div class="bill-row">
                        <span>Assigned Room:</span>
                        <strong>Room <%= booking.getRoomNumber() %> (<%= booking.getTypeName() %>)</strong>
                    </div>
                    <div class="bill-row">
                        <span>Check-In Date:</span>
                        <strong class="mono"><%= booking.getCheckInDate() %></strong>
                    </div>
                    <div class="bill-row">
                        <span>Check-Out Date:</span>
                        <strong class="mono"><%= booking.getCheckOutDate() %></strong>
                    </div>
                    <div class="bill-row">
                        <span>Payment Status:</span>
                        <span class="badge badge-success">PAID & SETTLED</span>
                    </div>

                    <div class="bill-total-highlight">
                        <span style="font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Total Paid Amount:</span>
                        <span class="mono" style="font-size: 1.2rem; font-weight: 700; color: #ffffff;">₹<%= booking.getTotalAmount() %></span>
                    </div>
                </div>

                <div class="alert alert-info" style="margin-bottom: var(--space-6);">
                    <span>ℹ</span>
                    <div>
                        <strong>Automated Housekeeping Dispatch:</strong> Completing checkout will update Room <%= booking.getRoomNumber() %> status to <span class="mono">CLEANING</span> and automatically route a Housekeeping task to available floor attendants.
                    </div>
                </div>

                <form action="checkout" method="POST">
                    <input type="hidden" name="bookingId" value="<%= booking.getId() %>">
                    <button type="submit" class="btn btn-primary" style="width: 100%; height: 42px;">
                        Complete Checkout & Release Room →
                    </button>
                </form>
            <% } else { %>
                <div style="text-align: center; padding: var(--space-8); color: var(--text-secondary);">
                    No active booking found. <a href="my-bookings">Return to My Bookings</a>
                </div>
            <% } %>
        </div>
    </main>

</body>
</html>
