<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hotel.model.User" %>
<%@ page import="com.hotel.model.Booking" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }
    Booking activeBooking = (Booking) request.getAttribute("activeBooking");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Guest Portal — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        .stay-overview-card {
            background-color: var(--bg-surface);
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-md);
            padding: var(--space-6);
            margin-bottom: var(--space-8);
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: var(--space-4);
        }
        .guest-services-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 20px;
            margin-top: 16px;
            margin-bottom: 40px;
        }
        @media (max-width: 992px) {
            .guest-services-grid {
                grid-template-columns: repeat(2, 1fr);
            }
        }
        @media (max-width: 640px) {
            .guest-services-grid {
                grid-template-columns: 1fr;
            }
        }
        .guest-card-tile {
            display: block;
            border-radius: 18px;
            overflow: hidden;
            border: 1px solid rgba(255, 255, 255, 0.08);
            background: #080808;
            transition: transform 0.25s cubic-bezier(0.16, 1, 0.3, 1), 
                        border-color 0.25s ease, 
                        box-shadow 0.25s ease;
            text-decoration: none;
            cursor: pointer;
            box-shadow: 0 4px 14px rgba(0, 0, 0, 0.5);
        }
        .guest-card-tile:hover {
            transform: translateY(-4px) scale(1.015);
            border-color: rgba(212, 175, 55, 0.45);
            box-shadow: 0 16px 36px rgba(0, 0, 0, 0.85), 0 0 24px rgba(212, 175, 55, 0.2);
        }
        .guest-card-img {
            width: 100%;
            height: auto;
            display: block;
            object-fit: cover;
            aspect-ratio: 327 / 196;
            transition: filter 0.25s ease;
        }
        .guest-card-tile:hover .guest-card-img {
            filter: brightness(1.06);
        }
        .service-tile-top {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: var(--space-3);
        }
        .service-tile-badge {
            font-size: 0.72rem;
            color: var(--text-muted);
            text-transform: uppercase;
            font-weight: 600;
            letter-spacing: 0.05em;
        }
        .service-tile-title {
            font-size: 1.05rem;
            font-weight: 600;
            color: #ffffff;
            margin-bottom: var(--space-1);
            letter-spacing: -0.02em;
        }
        .service-tile-desc {
            font-size: 0.82rem;
            color: var(--text-secondary);
            line-height: 1.45;
        }
        .service-tile-action {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 0.8rem;
            color: var(--text-secondary);
            font-weight: 500;
            margin-top: var(--space-4);
            transition: color var(--transition-fast);
        }
        .service-tile:hover .service-tile-action {
            color: #ffffff;
        }
    </style>
</head>
<body>

    <!-- Vercel Dual-Rail Header -->
    <header class="app-navbar">
        <div class="navbar-top">
            <div style="display: flex; align-items: center; gap: var(--space-3);">
                <a href="dashboard" class="brand-badge">
                    <img src="images/velora-mark.png" alt="Velora" class="brand-logo-img" style="mix-blend-mode: screen; width: 28px; height: 28px; object-fit: contain;">
                    <span style="font-weight: 700; letter-spacing: 0.05em; font-size: 0.95rem;">Velora</span>
                </a>
                <span class="brand-separator">/</span>
                <span class="brand-project-switcher">Guest Portal</span>
                <span class="brand-env-badge">Production</span>
            </div>
            <div class="nav-actions">
                <div class="user-profile-chip">
                    <span class="user-status-dot"></span>
                    <span>Guest: <strong><%= currentUser.getUsername() %></strong></span>
                </div>
                <a href="logout" class="btn btn-secondary btn-sm">Logout</a>
            </div>
        </div>

        <nav class="navbar-tabs">
            <a href="dashboard" class="nav-tab-item active">Overview</a>
            <a href="search-rooms" class="nav-tab-item">Search & Book</a>
            <a href="my-bookings" class="nav-tab-item">My Stays</a>
            <a href="service-request" class="nav-tab-item">Room Service</a>
            <a href="complaint" class="nav-tab-item">Report Issue</a>
            <a href="assistant" class="nav-tab-item">AI Concierge</a>
            <a href="billing" class="nav-tab-item">Billing & Invoices</a>
            <a href="review" class="nav-tab-item">Reviews</a>
        </nav>
    </header>

    <main class="app-container">

        <!-- Page Header -->
        <div class="page-header">
            <div>
                <h1 class="page-title">Welcome back, <%= currentUser.getUsername() %></h1>
                <p class="page-subtitle">Manage reservations, request smart room amenities, query 24/7 AI Concierge, and view verified digital invoices.</p>
            </div>
            <div style="display: flex; gap: var(--space-2); flex-wrap: wrap;">
                <a href="search-rooms" class="btn btn-primary btn-sm">+ Book New Stay</a>
                <a href="assistant" class="btn btn-secondary btn-sm">✨ Ask Velora AI</a>
            </div>
        </div>

        <!-- Active Stay Overview Card -->
        <div class="stay-overview-card">
            <div>
                <div style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-muted); font-weight: 600; letter-spacing: 0.05em; margin-bottom: 4px;">
                    Current Stay Telemetry
                </div>
                <% if (activeBooking != null) { %>
                    <div style="font-size: 1.25rem; font-weight: 700; color: #ffffff;">
                        Room <%= activeBooking.getRoomNumber() %> <span style="font-size: 0.95rem; font-weight: 400; color: var(--text-secondary);">(<%= activeBooking.getTypeName() %>)</span>
                    </div>
                    <div style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 4px;">
                        Booking Reference: <span class="mono" style="color: #ffffff;"><%= activeBooking.getBookingCode() %></span>
                        • Stay: <span class="mono"><%= activeBooking.getCheckInDate() %> to <%= activeBooking.getCheckOutDate() %></span>
                    </div>
                <% } else { %>
                    <div style="font-size: 1.1rem; font-weight: 600; color: #ffffff;">
                        No Active Checked-In Stay
                    </div>
                    <div style="font-size: 0.85rem; color: var(--text-muted); margin-top: 2px;">
                        You do not have a room checked-in at this moment. You can browse and book available suites below.
                    </div>
                <% } %>
            </div>

            <div>
                <% if (activeBooking != null) { %>
                    <span class="badge badge-success" style="padding: 6px 12px; font-size: 0.8rem;">
                        ● OCCUPIED & ACTIVE
                    </span>
                    <a href="billing?bookingId=<%= activeBooking.getId() %>" class="btn btn-secondary btn-sm" style="margin-left: var(--space-2);">
                        View Statement
                    </a>
                <% } else { %>
                    <a href="search-rooms" class="btn btn-primary btn-sm">Search Available Rooms</a>
                <% } %>
            </div>
        </div>

        <!-- Services Grid with 3D Luxury Illustrations -->
        <div style="margin-bottom: var(--space-8);">
            <div style="font-size: 0.85rem; font-weight: 600; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: var(--space-4);">
                Guest Services & Autonomous Operations
            </div>

            <div class="guest-services-grid">
                <a href="search-rooms" class="guest-card-tile" title="Find & Book Rooms">
                    <img src="images/card-rooms.png" alt="Find & Book Rooms" class="guest-card-img">
                </a>

                <a href="my-bookings" class="guest-card-tile" title="My Reservations & Invoices">
                    <img src="images/card-bookings.png" alt="My Reservations & Invoices" class="guest-card-img">
                </a>

                <a href="service-request" class="guest-card-tile" title="Order Room Service">
                    <img src="images/card-roomservice.png" alt="Order Room Service" class="guest-card-img">
                </a>

                <a href="complaint" class="guest-card-tile" title="Report an Issue">
                    <img src="images/card-complaint.png" alt="Report an Issue" class="guest-card-img">
                </a>

                <a href="assistant" class="guest-card-tile" title="Velora AI Concierge">
                    <img src="images/card-assistant.png" alt="Velora AI Concierge" class="guest-card-img">
                </a>

                <a href="billing" class="guest-card-tile" title="Billing & Tax Invoices">
                    <img src="images/card-billing.png" alt="Billing & Tax Invoices" class="guest-card-img">
                </a>

                <a href="review" class="guest-card-tile" title="Rate & Review Stay">
                    <img src="images/card-review.png" alt="Rate & Review Stay" class="guest-card-img">
                </a>
            </div>
        </div>

    </main>

</body>
</html>