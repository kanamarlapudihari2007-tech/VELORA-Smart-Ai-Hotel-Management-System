<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hotel.model.DynamicPricingRecommendation" %>
<%@ page import="com.hotel.model.RevenueOptimizationReport" %>
<%@ page import="com.hotel.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }

    RevenueOptimizationReport report = (RevenueOptimizationReport) request.getAttribute("report");
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI Dynamic Pricing & Revenue Optimization (RMS) — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        .strategy-banner-card {
            background: linear-gradient(135deg, var(--bg-card), var(--bg-card-sub));
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-xl);
            padding: var(--space-6);
            margin-bottom: var(--space-8);
            box-shadow: var(--shadow-md);
            position: relative;
            overflow: hidden;
        }
        .strategy-banner-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, var(--primary), var(--accent), var(--gold));
        }
        .market-signal-card {
            background: var(--bg-surface);
            border: 1px solid var(--border-subtle);
            border-radius: var(--radius-md);
            padding: var(--space-4);
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
                <span class="brand-project-switcher">RMS Revenue Engine</span>
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
            <a href="rooms" class="nav-tab-item">Room Inventory</a>
            <a href="my-bookings" class="nav-tab-item">Bookings Ledger</a>
            <a href="billing" class="nav-tab-item">Billing & Invoices</a>
            <a href="dynamic-pricing" class="nav-tab-item active">Dynamic Pricing</a>
            <a href="analytics" class="nav-tab-item">Analytics Briefing</a>
            <a href="manager-reviews" class="nav-tab-item">Reviews & Sentiment</a>
        </nav>
    </header>

    <main class="app-container">

        <div class="page-header">
            <div>
                <h1 class="page-title">AI Dynamic Pricing & Yield Management Engine</h1>
                <p class="page-subtitle">Algorithmic revenue optimization balancing occupancy elasticity, demand surge multipliers, and RevPAR maximization.</p>
            </div>
            <% if (report != null && report.getRecommendations() != null && !report.getRecommendations().isEmpty()) { %>
                <form action="apply-pricing" method="POST" style="margin:0;">
                    <input type="hidden" name="action" value="apply_all">
                    <button type="submit" class="btn btn-primary" onclick="return confirm('Apply all AI-recommended dynamic rates to live room inventory?');">
                        ⚡ Apply All AI Dynamic Rates
                    </button>
                </form>
            <% } %>
        </div>

        <% if ("applied_all".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>All AI-recommended dynamic rates applied across hotel room inventory in real-time.</div>
            </div>
        <% } else if ("applied_single".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Dynamic rate successfully applied to selected room category.</div>
            </div>
        <% } %>

        <% if (report != null) { %>

            <!-- Live Revenue KPIs -->
            <div class="metrics-grid">
                <div class="metric-card">
                    <div class="metric-value"><%= String.format("%.1f", report.getOverallOccupancyRate()) %>%</div>
                    <div class="metric-label">Current Hotel Occupancy</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value mono"><%= report.getTotalRooms() %></div>
                    <div class="metric-label">Total Hotel Rooms</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value success mono"><%= report.getOccupiedRooms() %></div>
                    <div class="metric-label">Occupied Rooms</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value mono" style="color: var(--primary);"><%= report.getAvailableRooms() %></div>
                    <div class="metric-label">Available Free Rooms</div>
                </div>
            </div>

            <!-- Gemini AI Strategic Narrative -->
            <div class="strategy-banner-card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-4); flex-wrap: wrap; gap: var(--space-3);">
                    <div style="font-size: 1.25rem; font-weight: 800; color: var(--text-primary); max-width: 780px;">
                        <%= report.getOverallStrategyHeadline() != null ? report.getOverallStrategyHeadline() : "Yield Strategy Active" %>
                    </div>
                    <span class="badge badge-info">
                        <%= report.isFromAI() ? "✨ Google Gemini AI RMS Strategy" : "⚙️ Rule-Based Yield Engine" %>
                    </span>
                </div>

                <div style="color: var(--text-secondary); font-size: 0.95rem; line-height: 1.65; margin-bottom: var(--space-5);">
                    <%= report.getAiMarketSummary() != null ? report.getAiMarketSummary() : "Monitoring room availability and pricing demand elasticity..." %>
                </div>

                <!-- Market Signals Grid -->
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: var(--space-3);">
                    <div class="market-signal-card">
                        <div style="font-size: 0.72rem; color: var(--text-muted); text-transform: uppercase; font-weight: 700;">Day of Week</div>
                        <div style="font-size: 1.1rem; font-weight: 700; color: var(--primary); margin-top: 2px;">
                            <%= report.getDayOfWeek() != null ? report.getDayOfWeek() : "Today" %>
                        </div>
                    </div>
                    <div class="market-signal-card">
                        <div style="font-size: 0.72rem; color: var(--text-muted); text-transform: uppercase; font-weight: 700;">Weekend Factor</div>
                        <div style="font-size: 1.1rem; font-weight: 700; color: var(--primary); margin-top: 2px;">
                            <%= report.isWeekend() ? "Weekend Surge Leisure" : "Weekday Business Standard" %>
                        </div>
                    </div>
                    <div class="market-signal-card">
                        <div style="font-size: 0.72rem; color: var(--text-muted); text-transform: uppercase; font-weight: 700;">Generated Timestamp</div>
                        <div class="mono" style="font-size: 0.9rem; font-weight: 700; color: var(--text-secondary); margin-top: 2px;">
                            <%= report.getGeneratedTimestamp() != null ? report.getGeneratedTimestamp() : "Live Telemetry" %>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Dynamic Pricing Matrix Table -->
            <div class="card" style="margin-bottom: var(--space-8);">
                <div class="card-header">
                    <div>
                        <h2 class="card-title" style="display: flex; align-items: center; gap: 8px;">
                            <span>🏷️</span> Room Category Dynamic Rate Matrix
                        </h2>
                        <p style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 2px;">
                            Compare baseline tariffs against AI recommended rates with individual or bulk application.
                        </p>
                    </div>
                </div>

                <div class="table-container">
                    <table class="table-modern">
                        <thead>
                            <tr>
                                <th>Category</th>
                                <th>Occupancy</th>
                                <th>Current Rate</th>
                                <th>AI Suggested Rate</th>
                                <th>Adjustment</th>
                                <th>Economic Rationale</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (report.getRecommendations() != null) {
                                for (DynamicPricingRecommendation rec : report.getRecommendations()) { 
                                    boolean isSurge = rec.getAdjustmentPercent() > 0;
                            %>
                                <tr>
                                    <td><strong style="font-size: 1.05rem; color: var(--text-primary);"><%= rec.getTypeName() %></strong></td>
                                    <td>
                                        <div style="font-weight: 600;"><%= String.format("%.0f", rec.getOccupancyRate()) %>%</div>
                                        <div style="font-size: 0.75rem; color: var(--text-muted);"><%= rec.getOccupiedRooms() %> of <%= rec.getTotalRooms() %> booked</div>
                                    </td>
                                    <td><span class="mono" style="color: var(--text-muted);">₹<%= rec.getCurrentPrice() %></span></td>
                                    <td><span class="mono" style="font-size: 1.15rem; font-weight: 800; color: var(--primary);">₹<%= rec.getRecommendedPrice() %></span></td>
                                    <td>
                                        <span class="badge <%= isSurge ? "badge-success" : "badge-info" %> mono">
                                            <%= isSurge ? "▲ +" : "▼ " %><%= String.format("%.1f", rec.getAdjustmentPercent()) %>%
                                        </span>
                                    </td>
                                    <td style="max-width: 250px; font-size: 0.85rem; color: var(--text-secondary);">
                                        <%= rec.getEconomicRationale() %>
                                    </td>
                                    <td>
                                        <form action="apply-pricing" method="POST" style="margin:0;">
                                            <input type="hidden" name="action" value="apply_single">
                                            <input type="hidden" name="roomTypeId" value="<%= rec.getRoomTypeId() %>">
                                            <input type="hidden" name="newPrice" value="<%= rec.getRecommendedPrice() %>">
                                            <button type="submit" class="btn btn-secondary btn-sm">
                                                Apply Rate
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            <%  }
                               } %>
                        </tbody>
                    </table>
                </div>
            </div>

        <% } else { %>
            <div class="card" style="text-align: center; padding: var(--space-8); color: var(--text-muted);">
                Revenue report data is currently initializing. Please refresh in a moment.
            </div>
        <% } %>

    </main>

</body>
</html>
