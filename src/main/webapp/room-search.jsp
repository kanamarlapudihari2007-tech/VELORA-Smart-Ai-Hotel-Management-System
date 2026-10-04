<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="com.hotel.model.Room" %>
<%@ page import="com.hotel.model.RoomType" %>
<%@ page import="com.hotel.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    List<Room> rooms = (List<Room>) request.getAttribute("rooms");
    List<RoomType> roomTypes = (List<RoomType>) request.getAttribute("roomTypes");

    String checkInDate = (String) request.getAttribute("checkInDate");
    String checkOutDate = (String) request.getAttribute("checkOutDate");
    Integer selectedRoomTypeId = (Integer) request.getAttribute("selectedRoomTypeId");
    Long numberOfNights = (Long) request.getAttribute("numberOfNights");
    if (numberOfNights == null || numberOfNights < 1) numberOfNights = 1L;
    boolean isManager = currentUser != null && "MANAGER".equals(currentUser.getRole());
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Room Search & Availability — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        .search-hero-card {
            background: var(--bg-surface);
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-md);
            padding: var(--space-6);
            margin-bottom: var(--space-8);
        }
        .search-grid-form {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(210px, 1fr)) 150px;
            gap: var(--space-4);
            align-items: flex-end;
            margin-top: var(--space-4);
        }
        .room-inventory-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
            gap: var(--space-4);
        }
        .room-card-box {
            background: var(--bg-surface);
            border: 1px solid var(--border-subtle);
            border-radius: var(--radius-md);
            padding: var(--space-6);
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            transition: border-color var(--transition-fast), background var(--transition-fast);
        }
        .room-card-box:hover {
            border-color: var(--border-medium);
            background: var(--bg-card-hover);
        }
        .room-price-strip {
            background: #000000;
            border: 1px solid var(--border-subtle);
            border-radius: var(--radius-sm);
            padding: var(--space-3) var(--space-4);
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin: var(--space-4) 0;
        }
    </style>
</head>
<body>

    <!-- Vercel Dual-Rail Header -->
    <header class="app-navbar">
        <div class="navbar-top">
            <div style="display: flex; align-items: center; gap: var(--space-3);">
                <a href="<%= currentUser != null ? (isManager ? "manager-tasks" : "dashboard") : "./" %>" class="brand-badge">
                    <img src="images/velora-mark.png" alt="Velora" class="brand-logo-img">
                    <span>Velora</span>
                </a>
                <span class="brand-separator">/</span>
                <span class="brand-project-switcher">Reservations</span>
                <span class="brand-env-badge">Production</span>
            </div>
            <div class="nav-actions">
                <% if (currentUser != null) { %>
                    <div class="user-profile-chip">
                        <span class="user-status-dot"></span>
                        <span><%= currentUser.getUsername() %></span>
                    </div>
                    <a href="logout" class="btn btn-secondary btn-sm">Logout</a>
                <% } else { %>
                    <a href="login.jsp" class="btn btn-primary btn-sm">Sign In →</a>
                <% } %>
            </div>
        </div>

        <nav class="navbar-tabs">
            <% if (isManager) { %>
                <a href="manager-tasks" class="nav-tab-item">Tasks & Dispatch</a>
                <a href="rooms" class="nav-tab-item">Room Inventory</a>
                <a href="my-bookings" class="nav-tab-item">Bookings Ledger</a>
                <a href="billing" class="nav-tab-item">Billing & Invoices</a>
                <a href="dynamic-pricing" class="nav-tab-item">Dynamic Pricing</a>
                <a href="analytics" class="nav-tab-item">Analytics Briefing</a>
                <a href="manager-reviews" class="nav-tab-item">Reviews & Sentiment</a>
            <% } else if (currentUser != null) { %>
                <a href="dashboard" class="nav-tab-item">Overview</a>
                <a href="search-rooms" class="nav-tab-item active">Search & Book</a>
                <a href="my-bookings" class="nav-tab-item">My Stays</a>
                <a href="service-request" class="nav-tab-item">Room Service</a>
                <a href="complaint" class="nav-tab-item">Report Issue</a>
                <a href="assistant" class="nav-tab-item">AI Concierge</a>
                <a href="billing" class="nav-tab-item">Billing & Invoices</a>
                <a href="review" class="nav-tab-item">Reviews</a>
            <% } else { %>
                <a href="./" class="nav-tab-item">Platform Overview</a>
                <a href="search-rooms" class="nav-tab-item active">Search & Book</a>
                <a href="login.jsp" class="nav-tab-item">Guest Portal</a>
                <a href="login.jsp" class="nav-tab-item">Manager Console</a>
            <% } %>
        </nav>
    </header>

    <main class="app-container">

        <!-- Search Bar Card -->
        <div class="search-hero-card">
            <h2 style="font-size: 1.15rem; font-weight: 600; color: #ffffff; letter-spacing: -0.02em;">
                Find Available Suites & Rooms
            </h2>
            <p style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 4px;">
                Select your check-in and check-out dates to review real-time availability and guaranteed rates.
            </p>

            <form action="search-rooms" method="GET" class="search-grid-form">
                <div class="form-group" style="margin-bottom: 0;">
                    <label for="checkInDate" class="form-label">Check-In Date *</label>
                    <input type="date" id="checkInDate" name="checkInDate" class="form-control" value="<%= checkInDate != null ? checkInDate : "" %>" required>
                </div>

                <div class="form-group" style="margin-bottom: 0;">
                    <label for="checkOutDate" class="form-label">Check-Out Date *</label>
                    <input type="date" id="checkOutDate" name="checkOutDate" class="form-control" value="<%= checkOutDate != null ? checkOutDate : "" %>" required>
                </div>

                <div class="form-group" style="margin-bottom: 0;">
                    <label for="roomTypeId" class="form-label">Room Category</label>
                    <select id="roomTypeId" name="roomTypeId" class="form-control">
                        <option value="">All Categories</option>
                        <% if (roomTypes != null) {
                            for (RoomType type : roomTypes) { 
                                boolean isSelected = (selectedRoomTypeId != null && selectedRoomTypeId == type.getId());
                        %>
                            <option value="<%= type.getId() %>" <%= isSelected ? "selected" : "" %>>
                                <%= type.getTypeName() %> (₹<%= type.getPricePerNight() %>/night)
                            </option>
                        <%  }
                           } %>
                    </select>
                </div>

                <div>
                    <button type="submit" class="btn btn-primary" style="width: 100%; height: 38px;">Search Rooms</button>
                </div>
            </form>
        </div>

        <!-- Search Results Header -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-4); flex-wrap: wrap; gap: var(--space-2);">
            <div style="font-size: 1rem; font-weight: 600; color: #ffffff;">
                Available Accommodations (<%= rooms != null ? rooms.size() : 0 %> found)
            </div>
            <span class="badge badge-neutral">Stay Duration: <%= numberOfNights %> Night(s)</span>
        </div>

        <% if (rooms != null && !rooms.isEmpty()) { %>
            <div class="room-inventory-grid">
                <% for (Room room : rooms) { 
                    BigDecimal totalStayCost = room.getPricePerNight().multiply(new BigDecimal(numberOfNights));
                %>
                    <div class="room-card-box">
                        <div>
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: var(--space-2);">
                                <h3 style="font-size: 1.15rem; font-weight: 600; color: #ffffff;">Room <%= room.getRoomNumber() %></h3>
                                <span class="badge badge-neutral">Floor <%= room.getFloorNumber() %></span>
                            </div>

                            <div style="font-size: 0.95rem; font-weight: 600; color: #ededed; margin-bottom: var(--space-2);">
                                <%= room.getTypeName() %>
                            </div>

                            <div style="color: var(--text-secondary); font-size: 0.82rem; line-height: 1.5;">
                                <div>Capacity: <strong>Up to <%= room.getCapacity() %> Guests</strong></div>
                                <div>High-speed Wi-Fi, Climate Control & Smart Amenities</div>
                            </div>
                        </div>

                        <div>
                            <div class="room-price-strip">
                                <div>
                                    <div style="font-size: 0.7rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600; letter-spacing: 0.05em;">Nightly Rate</div>
                                    <div class="mono" style="font-size: 1.1rem; font-weight: 700; color: #ffffff;">₹<%= room.getPricePerNight() %></div>
                                </div>
                                <div style="text-align: right;">
                                    <div style="font-size: 0.7rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600; letter-spacing: 0.05em;">Total (<%= numberOfNights %> night<%= numberOfNights > 1 ? "s" : "" %>)</div>
                                    <div class="mono" style="font-size: 1.1rem; font-weight: 700; color: #ffffff;">₹<%= totalStayCost %></div>
                                </div>
                            </div>

                            <% if (currentUser != null) { %>
                                <a href="book-room?roomId=<%= room.getId() %>&checkIn=<%= checkInDate %>&checkOut=<%= checkOutDate %>" class="btn btn-primary" style="width: 100%;">
                                    Book Room Now →
                                </a>
                            <% } else { %>
                                <a href="login.jsp" class="btn btn-primary" style="width: 100%;">
                                    Sign In to Book →
                                </a>
                            <% } %>
                        </div>
                    </div>
                <% } %>
            </div>
        <% } else { %>
            <div class="card" style="text-align: center; padding: var(--space-10) var(--space-6);">
                <div style="font-size: 1.25rem; font-weight: 600; color: #ffffff; margin-bottom: var(--space-2);">No Available Rooms Found</div>
                <p style="color: var(--text-secondary); max-width: 480px; margin: 0 auto; font-size: 0.85rem; line-height: 1.5;">
                    No rooms are currently unreserved for the requested dates and category. Please adjust your stay dates or select 'All Categories'.
                </p>
            </div>
        <% } %>

    </main>

</body>
</html>
