<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hotel.model.User" %>
<%@ page import="com.hotel.model.HotelAnalytics" %>
<%@ page import="com.hotel.model.Staff" %>
<%@ page import="java.util.Map" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }

    HotelAnalytics analytics = (HotelAnalytics) request.getAttribute("analytics");
    if (analytics == null) {
        analytics = new HotelAnalytics();
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Executive Operations Analytics — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        .executive-hero-card {
            background: linear-gradient(135deg, var(--bg-card), var(--bg-card-sub));
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-xl);
            padding: var(--space-6);
            margin-bottom: var(--space-8);
            box-shadow: var(--shadow-lg);
            position: relative;
            overflow: hidden;
        }
        .executive-hero-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, var(--primary), #6366f1, #ec4899);
        }
        .ai-blocks-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: var(--space-4);
            margin-top: var(--space-5);
        }
        .ai-sub-block {
            background: var(--bg-surface);
            border: 1px solid var(--border-subtle);
            border-radius: var(--radius-md);
            padding: var(--space-4);
        }
        .progress-bar-track {
            width: 100%;
            height: 8px;
            background: var(--bg-canvas);
            border-radius: var(--radius-full);
            overflow: hidden;
            margin: var(--space-2) 0;
            border: 1px solid var(--border-subtle);
        }
        .progress-bar-indicator {
            height: 100%;
            border-radius: var(--radius-full);
            background: linear-gradient(90deg, var(--primary), var(--accent));
        }
        .analytics-split-layout {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: var(--space-6);
            margin-bottom: var(--space-8);
        }
        @media (max-width: 900px) {
            .analytics-split-layout {
                grid-template-columns: 1fr;
            }
        }
        .rank-circle {
            width: 24px;
            height: 24px;
            border-radius: 50%;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 0.75rem;
            font-weight: 800;
        }
        .rank-1 { background: var(--gold); color: #000; }
        .rank-2 { background: var(--text-secondary); color: #000; }
        .rank-3 { background: #b45309; color: #fff; }
        .rank-n { background: var(--bg-surface); color: var(--text-secondary); border: 1px solid var(--border-medium); }
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
                <span class="brand-project-switcher">Executive Intelligence</span>
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
            <a href="dynamic-pricing" class="nav-tab-item">Dynamic Pricing</a>
            <a href="analytics" class="nav-tab-item active">Analytics Briefing</a>
            <a href="manager-reviews" class="nav-tab-item">Reviews & Sentiment</a>
        </nav>
    </header>

    <main class="app-container">

        <div class="page-header">
            <div>
                <h1 class="page-title">Hotel Operations Analytics & Executive Briefing</h1>
                <p class="page-subtitle">Multi-dimensional operational telemetry, room asset utilization, financial pace, and automated Gemini executive analysis.</p>
            </div>
            <a href="analytics?refreshAi=true" class="btn btn-primary btn-sm" id="refreshAiBtn">
                <span>⚡</span> Regenerate AI Briefing
            </a>
        </div>

        <!-- AI Executive Briefing Hero Card -->
        <div class="executive-hero-card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-3); flex-wrap: wrap; gap: var(--space-2);">
                <span class="badge badge-info">
                    <%= analytics.isFromAI() ? "✨ Google Gemini AI Executive Briefing" : "⚙️ Operations Intelligence Engine" %>
                </span>
                <span class="mono" style="font-size: 0.8rem; color: var(--text-muted);">
                    Timestamp: <%= analytics.getGeneratedTimestamp() != null ? analytics.getGeneratedTimestamp() : "Live" %>
                </span>
            </div>

            <div style="font-size: 1.3rem; font-weight: 800; color: var(--text-primary); margin-bottom: var(--space-3); line-height: 1.4;">
                <%= analytics.getAiExecutiveHeadline() != null ? analytics.getAiExecutiveHeadline() : "Synthesizing Operational Data..." %>
            </div>

            <div class="ai-blocks-grid">
                <div class="ai-sub-block">
                    <div style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--primary); letter-spacing: 0.05em; margin-bottom: var(--space-2); display: flex; align-items: center; gap: 6px;">
                        <span>📈</span> Operational Overview
                    </div>
                    <div style="font-size: 0.9rem; color: var(--text-secondary); line-height: 1.6; white-space: pre-line;">
                        <%= analytics.getAiOperationalBriefing() != null ? analytics.getAiOperationalBriefing() : "Loading operational overview..." %>
                    </div>
                </div>

                <div class="ai-sub-block">
                    <div style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--danger); letter-spacing: 0.05em; margin-bottom: var(--space-2); display: flex; align-items: center; gap: 6px;">
                        <span>⚠️</span> Risk & Bottleneck Alerts
                    </div>
                    <div style="font-size: 0.9rem; color: var(--text-secondary); line-height: 1.6; white-space: pre-line;">
                        <%= analytics.getAiRiskAlerts() != null ? analytics.getAiRiskAlerts() : "No critical risks detected." %>
                    </div>
                </div>

                <div class="ai-sub-block">
                    <div style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--success); letter-spacing: 0.05em; margin-bottom: var(--space-2); display: flex; align-items: center; gap: 6px;">
                        <span>💡</span> Actionable Manager Recommendations
                    </div>
                    <div style="font-size: 0.9rem; color: var(--text-secondary); line-height: 1.6; white-space: pre-line;">
                        <%= analytics.getAiActionableRecommendations() != null ? analytics.getAiActionableRecommendations() : "Maintain proactive monitoring." %>
                    </div>
                </div>
            </div>
        </div>

        <!-- Metrics KPI Grid -->
        <div class="metrics-grid">
            <div class="metric-card">
                <div class="metric-value"><%= analytics.getOccupancyRate() %>%</div>
                <div class="metric-label">Current Hotel Occupancy</div>
                <div class="progress-bar-track">
                    <div class="progress-bar-indicator" style="width: <%= Math.min(100.0, analytics.getOccupancyRate()) %>%;"></div>
                </div>
                <div style="font-size: 0.78rem; color: var(--text-muted); display: flex; gap: 6px; flex-wrap: wrap;">
                    <span><strong><%= analytics.getOccupiedRooms() %></strong> Occupied</span> •
                    <span><strong><%= analytics.getReservedRooms() %></strong> Reserved</span> •
                    <span style="color: var(--success);"><strong><%= analytics.getAvailableRooms() %></strong> Free</span>
                </div>
            </div>

            <div class="metric-card">
                <div class="metric-value mono">₹<%= String.format("%,.0f", analytics.getTotalRevenue()) %></div>
                <div class="metric-label">Total Booked Revenue</div>
                <div class="progress-bar-track">
                    <% 
                        double paidPct = (analytics.getTotalRevenue() > 0) ? (analytics.getPaidRevenue() / analytics.getTotalRevenue()) * 100.0 : 0.0;
                    %>
                    <div class="progress-bar-indicator" style="width: <%= Math.min(100.0, paidPct) %>%; background: var(--success);"></div>
                </div>
                <div style="font-size: 0.78rem; color: var(--text-muted); display: flex; gap: 6px; flex-wrap: wrap;">
                    <span style="color: var(--success);">Settled: ₹<%= String.format("%,.0f", analytics.getPaidRevenue()) %></span> •
                    <span style="color: var(--warning);">Pending: ₹<%= String.format("%,.0f", analytics.getPendingRevenue()) %></span>
                </div>
            </div>

            <div class="metric-card">
                <div class="metric-value"><%= analytics.getActiveCheckInsCount() %></div>
                <div class="metric-label">In-House Active Stays</div>
                <div style="margin-top: var(--space-3); font-size: 0.8rem; color: var(--text-secondary);">
                    Out of <strong style="color: var(--text-primary);"><%= analytics.getTotalBookingsCount() %></strong> Total Reservations
                </div>
                <div style="font-size: 0.75rem; color: var(--success); margin-top: 2px;">
                    ● Active Stays Monitored
                </div>
            </div>

            <div class="metric-card">
                <div class="metric-value <%= analytics.getUrgentComplaints() > 0 ? "danger" : "" %>">
                    <%= analytics.getTotalComplaints() %>
                </div>
                <div class="metric-label">Complaints & Escalations</div>
                <div style="margin-top: var(--space-3); font-size: 0.8rem; color: var(--text-secondary);">
                    <span style="color: <%= analytics.getUrgentComplaints() > 0 ? "var(--danger)" : "var(--success)" %>; font-weight: 700;">
                        <%= analytics.getUrgentComplaints() %> Urgent / High
                    </span> •
                    <span><%= analytics.getResolvedComplaints() %> Resolved</span>
                </div>
            </div>
        </div>

        <!-- Two Column Deep Dive Layout -->
        <div class="analytics-split-layout">
            
            <!-- Department Issue Distribution -->
            <div class="card">
                <div class="card-header">
                    <h2 class="card-title" style="display: flex; align-items: center; gap: 8px;">
                        <span>🏢</span> Issue Distribution by Sector
                    </h2>
                    <span class="badge badge-neutral"><%= analytics.getTotalComplaints() %> Total</span>
                </div>
                <%
                    Map<String, Integer> catMap = analytics.getComplaintsByCategory();
                    int totalC = analytics.getTotalComplaints();
                    String[] cats = {"Maintenance", "Housekeeping", "Food Service", "Reception", "Security"};
                    String[] colors = {"#38bdf8", "#10b981", "#f59e0b", "#a855f7", "#ef4444"};
                    for (int i = 0; i < cats.length; i++) {
                        String c = cats[i];
                        int count = (catMap != null && catMap.containsKey(c)) ? catMap.get(c) : 0;
                        double pct = (totalC > 0) ? ((double) count / totalC) * 100.0 : 0.0;
                %>
                    <div style="margin-bottom: var(--space-3);">
                        <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 4px;">
                            <span style="font-weight: 600; color: var(--text-primary);"><%= c %></span>
                            <span style="color: var(--text-muted);"><%= count %> issues (<%= Math.round(pct) %>%)</span>
                        </div>
                        <div class="progress-bar-track" style="margin: 0;">
                            <div style="height: 100%; border-radius: var(--radius-full); width: <%= pct %>%; background: <%= colors[i] %>;"></div>
                        </div>
                    </div>
                <% } %>
            </div>

            <!-- Top Performing Staff Leaderboard -->
            <div class="card">
                <div class="card-header">
                    <h2 class="card-title" style="display: flex; align-items: center; gap: 8px;">
                        <span>🏆</span> Staff Efficiency Leaderboard
                    </h2>
                    <span class="badge badge-neutral"><%= analytics.getTotalStaffCount() %> Active</span>
                </div>
                <div class="table-container">
                    <table class="table-modern">
                        <thead>
                            <tr>
                                <th>Rank</th>
                                <th>Employee</th>
                                <th>Sector</th>
                                <th>Status</th>
                                <th>Credits</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                if (analytics.getTopStaff() != null && !analytics.getTopStaff().isEmpty()) {
                                    int rank = 1;
                                    for (Staff s : analytics.getTopStaff()) {
                                        String rankClass = (rank == 1) ? "rank-1" : (rank == 2) ? "rank-2" : (rank == 3) ? "rank-3" : "rank-n";
                            %>
                                <tr>
                                    <td><span class="rank-circle <%= rankClass %>"><%= rank++ %></span></td>
                                    <td><strong><%= s.getFullName() %></strong></td>
                                    <td><span class="badge badge-neutral"><%= s.getCategory() %></span></td>
                                    <td>
                                        <% if ("AVAILABLE".equalsIgnoreCase(s.getWorkStatus())) { %>
                                            <span style="color: var(--success); font-weight: 700; font-size: 0.8rem;">● Available</span>
                                        <% } else if ("BUSY".equalsIgnoreCase(s.getWorkStatus())) { %>
                                            <span style="color: var(--warning); font-weight: 700; font-size: 0.8rem;">● Busy</span>
                                        <% } else { %>
                                            <span style="color: var(--text-muted); font-size: 0.8rem;">○ Off Duty</span>
                                        <% } %>
                                    </td>
                                    <td><strong style="color: var(--gold);" class="mono">⭐ <%= s.getPerformanceCredits() %></strong></td>
                                </tr>
                            <%      }
                                } else {
                            %>
                                <tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: var(--space-6);">No staff records available.</td></tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>

    </main>

</body>
</html>
