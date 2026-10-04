<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="com.hotel.model.Room" %>
<%@ page import="com.hotel.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }

    Room room = (Room) request.getAttribute("room");
    String checkInDate = (String) request.getAttribute("checkInDate");
    String checkOutDate = (String) request.getAttribute("checkOutDate");
    Long numberOfNights = (Long) request.getAttribute("numberOfNights");
    BigDecimal totalAmount = (BigDecimal) request.getAttribute("totalAmount");
    String errorMessage = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Confirm Stay Reservation — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        .summary-box {
            background: #000000;
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-sm);
            padding: var(--space-5);
            margin-bottom: var(--space-6);
        }
        .summary-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: var(--space-2) 0;
            border-bottom: 1px solid var(--border-subtle);
            font-size: 0.85rem;
            color: var(--text-secondary);
        }
        .summary-row:last-child {
            border-bottom: none;
        }
        .summary-row strong {
            color: #ededed;
        }
        .summary-total {
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
                <span class="brand-project-switcher">Reservation Checkout</span>
                <span class="brand-env-badge">Production</span>
            </div>
            <div class="nav-actions">
                <a href="search-rooms" class="btn btn-secondary btn-sm">← Back to Search</a>
            </div>
        </div>

        <nav class="navbar-tabs">
            <a href="dashboard" class="nav-tab-item">Overview</a>
            <a href="search-rooms" class="nav-tab-item active">Search & Book</a>
            <a href="my-bookings" class="nav-tab-item">My Stays</a>
            <a href="service-request" class="nav-tab-item">Room Service</a>
            <a href="complaint" class="nav-tab-item">Report Issue</a>
            <a href="assistant" class="nav-tab-item">AI Concierge</a>
            <a href="billing" class="nav-tab-item">Billing & Invoices</a>
            <a href="review" class="nav-tab-item">Reviews</a>
        </nav>
    </header>

    <main class="app-container-sm">
        <div class="card">
            <div style="margin-bottom: var(--space-6);">
                <h1 style="font-size: 1.25rem; font-weight: 600; color: #ffffff; letter-spacing: -0.02em;">Confirm Stay Reservation</h1>
                <p style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 4px;">
                    Review your itinerary breakdown and select settlement terms to confirm room hold.
                </p>
            </div>

            <% if (errorMessage != null) { %>
                <div class="alert alert-danger" style="margin-bottom: var(--space-4);">
                    <span>✕</span>
                    <div><%= errorMessage %></div>
                </div>
            <% } %>

            <% if (room != null) { %>
                <div class="summary-box">
                    <div class="summary-row">
                        <span>Selected Room:</span>
                        <strong>Room <%= room.getRoomNumber() %> (Floor <%= room.getFloorNumber() %>)</strong>
                    </div>
                    <div class="summary-row">
                        <span>Category:</span>
                        <strong><%= room.getTypeName() %></strong>
                    </div>
                    <div class="summary-row">
                        <span>Check-In Date:</span>
                        <strong class="mono"><%= checkInDate %></strong>
                    </div>
                    <div class="summary-row">
                        <span>Check-Out Date:</span>
                        <strong class="mono"><%= checkOutDate %></strong>
                    </div>
                    <div class="summary-row">
                        <span>Stay Duration:</span>
                        <strong><%= numberOfNights %> Night(s)</strong>
                    </div>
                    <div class="summary-row">
                        <span>Nightly Base Tariff:</span>
                        <strong class="mono">₹<%= room.getPricePerNight() %></strong>
                    </div>

                    <div class="summary-total">
                        <span style="font-weight: 600; color: var(--text-secondary); font-size: 0.85rem;">Total Payable Amount:</span>
                        <span class="mono" style="font-size: 1.2rem; font-weight: 700; color: #ffffff;">₹<%= totalAmount %></span>
                    </div>
                </div>

                <form action="book-room" method="POST">
                    <input type="hidden" name="roomId" value="<%= room.getId() %>">
                    <input type="hidden" name="checkInDate" value="<%= checkInDate %>">
                    <input type="hidden" name="checkOutDate" value="<%= checkOutDate %>">

                    <div class="form-group" style="margin-bottom: var(--space-6);">
                        <label for="paymentMethod" class="form-label">Payment Settlement Channel</label>
                        <select id="paymentMethod" name="paymentMethod" class="form-control">
                            <option value="CARD">Credit / Debit Card (Online Pay)</option>
                            <option value="UPI">UPI Instant Pay (GPay / PhonePe / QR)</option>
                            <option value="NETBANKING">Internet Banking (Major Indian Banks)</option>
                            <option value="PAY_AT_HOTEL">Pay at Hotel Front Desk (Upon Check-In)</option>
                        </select>
                    </div>

                    <div style="display: flex; gap: var(--space-3); justify-content: flex-end;">
                        <a href="search-rooms" class="btn btn-secondary btn-md">Cancel</a>
                        <button type="submit" class="btn btn-primary btn-md">
                            Confirm & Reserve Suite →
                        </button>
                    </div>
                </form>
            <% } else { %>
                <div style="text-align: center; padding: var(--space-8); color: var(--text-secondary);">
                    No room details found. <a href="search-rooms">Return to Room Search</a>
                </div>
            <% } %>
        </div>
    </main>

</body>
</html>
