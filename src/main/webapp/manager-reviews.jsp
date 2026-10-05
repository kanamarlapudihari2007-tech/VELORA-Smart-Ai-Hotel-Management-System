<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="com.hotel.model.Review" %>
<%@ page import="com.hotel.model.User" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }

    Map<String, Object> metrics = (Map<String, Object>) request.getAttribute("metrics");
    List<Review> reviews = (List<Review>) request.getAttribute("reviews");

    int totalReviews = 0;
    double avgRating = 0.0;
    int posCount = 0;
    int escalatedCount = 0;

    int cleanScore = 90;
    int staffScore = 90;
    int roomScore = 90;
    int foodScore = 90;
    int valueScore = 90;

    if (metrics != null) {
        if (metrics.get("totalReviews") instanceof Number) {
            totalReviews = ((Number) metrics.get("totalReviews")).intValue();
        }
        if (metrics.get("avgRating") instanceof Number) {
            avgRating = ((Number) metrics.get("avgRating")).doubleValue();
        }
        if (metrics.get("positiveCount") instanceof Number) {
            posCount = ((Number) metrics.get("positiveCount")).intValue();
        }
        if (metrics.get("escalatedCount") instanceof Number) {
            escalatedCount = ((Number) metrics.get("escalatedCount")).intValue();
        }

        if (metrics.get("cleanlinessScore") instanceof Number) {
            cleanScore = ((Number) metrics.get("cleanlinessScore")).intValue();
        }
        if (metrics.get("staffScore") instanceof Number) {
            staffScore = ((Number) metrics.get("staffScore")).intValue();
        }
        if (metrics.get("roomScore") instanceof Number) {
            roomScore = ((Number) metrics.get("roomScore")).intValue();
        }
        if (metrics.get("foodScore") instanceof Number) {
            foodScore = ((Number) metrics.get("foodScore")).intValue();
        }
        if (metrics.get("valueScore") instanceof Number) {
            valueScore = ((Number) metrics.get("valueScore")).intValue();
        }
    }

    int posPercent = totalReviews > 0 ? (int) Math.round(((double) posCount / totalReviews) * 100.0) : 100;
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reputation & Aspect Sentiment Intelligence — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        :root {
            --clean-pct: <%= cleanScore %>%;
            --staff-pct: <%= staffScore %>%;
            --room-pct: <%= roomScore %>%;
            --food-pct: <%= foodScore %>%;
            --value-pct: <%= valueScore %>%;
        }
        .aspect-bar-container {
            background: var(--bg-surface);
            border: 1px solid var(--border-subtle);
            border-radius: var(--radius-lg);
            padding: var(--space-6);
            margin-bottom: var(--space-8);
        }
        .aspect-row-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: var(--space-5);
            margin-top: var(--space-4);
        }
        .aspect-item-box {
            display: flex;
            flex-direction: column;
            gap: var(--space-2);
        }
        .aspect-track {
            background: var(--bg-canvas);
            height: 8px;
            border-radius: var(--radius-full);
            overflow: hidden;
            border: 1px solid var(--border-subtle);
        }
        .aspect-fill {
            height: 100%;
            background: linear-gradient(90deg, var(--primary), var(--accent));
            border-radius: var(--radius-full);
            transition: width 0.8s ease-in-out;
        }
        .reply-drawer {
            background: rgba(14, 165, 233, 0.04);
            border: 1px solid rgba(14, 165, 233, 0.2);
            border-radius: var(--radius-md);
            padding: var(--space-4) var(--space-5);
            margin-top: var(--space-4);
        }
        .reply-published {
            background: rgba(16, 185, 129, 0.06);
            border: 1px solid var(--success-border);
            border-radius: var(--radius-md);
            padding: var(--space-4) var(--space-5);
            margin-top: var(--space-4);
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
                <span class="brand-project-switcher">Sentiment Intelligence</span>
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
            <a href="analytics" class="nav-tab-item">Analytics Briefing</a>
            <a href="manager-reviews" class="nav-tab-item active">Reviews & Sentiment</a>
        </nav>
    </header>

    <main class="app-container">

        <div class="page-header">
            <div>
                <h1 class="page-title">Guest Sentiment & Multi-Aspect Reputation Engine</h1>
                <p class="page-subtitle">Real-time AI sentiment audit across cleanliness, staff service, room comfort, food quality, and value perception.</p>
            </div>
        </div>

        <% if ("reply_published".equals(msg) || "reply_sent".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Official management reply published successfully! Verified guest has been notified.</div>
            </div>
        <% } else if ("invalid_reply".equals(msg) || "invalid_reply".equals(request.getParameter("error"))) { %>
            <div class="alert alert-danger">
                <span>⚠</span>
                <div>Unable to publish reply. Please provide non-empty feedback.</div>
            </div>
        <% } %>

        <!-- KPI Metrics Grid -->
        <div class="metrics-grid">
            <div class="metric-card">
                <div class="metric-value"><%= String.format("%.1f", avgRating) %><span style="font-size: 1.1rem; color: var(--gold);">★</span></div>
                <div class="metric-label">Average Guest Rating</div>
            </div>
            <div class="metric-card">
                <div class="metric-value"><%= totalReviews %></div>
                <div class="metric-label">Total Verified Reviews</div>
            </div>
            <div class="metric-card">
                <div class="metric-value success"><%= posPercent %>%</div>
                <div class="metric-label">Positive Sentiment Rate</div>
            </div>
            <div class="metric-card">
                <div class="metric-value <%= escalatedCount > 0 ? "danger" : "success" %>"><%= escalatedCount %></div>
                <div class="metric-label">Critical Escalations</div>
            </div>
        </div>

        <!-- Multi-Aspect Satisfaction Breakdown -->
        <div class="aspect-bar-container">
            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-subtle); padding-bottom: var(--space-4);">
                <div>
                    <h2 style="font-size: 1.15rem; font-weight: 800; color: var(--text-primary); display: flex; align-items: center; gap: 8px;">
                        <span>📊</span> AI Multi-Aspect Satisfaction Index
                    </h2>
                    <p style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 2px;">
                        Calculated from Google Gemini AI aspect classification across all verified guest folios.
                    </p>
                </div>
            </div>

            <div class="aspect-row-grid">
                <div class="aspect-item-box">
                    <div style="display: flex; justify-content: space-between; font-size: 0.85rem; font-weight: 700;">
                        <span>🧹 Cleanliness</span>
                        <span class="mono" style="color: var(--primary);"><%= cleanScore %>%</span>
                    </div>
                    <div class="aspect-track">
                        <div class="aspect-fill" style="width: var(--clean-pct);"></div>
                    </div>
                </div>

                <div class="aspect-item-box">
                    <div style="display: flex; justify-content: space-between; font-size: 0.85rem; font-weight: 700;">
                        <span>🤝 Staff Courteousness</span>
                        <span class="mono" style="color: var(--primary);"><%= staffScore %>%</span>
                    </div>
                    <div class="aspect-track">
                        <div class="aspect-fill" style="width: var(--staff-pct);"></div>
                    </div>
                </div>

                <div class="aspect-item-box">
                    <div style="display: flex; justify-content: space-between; font-size: 0.85rem; font-weight: 700;">
                        <span>🛌 Room Comfort</span>
                        <span class="mono" style="color: var(--primary);"><%= roomScore %>%</span>
                    </div>
                    <div class="aspect-track">
                        <div class="aspect-fill" style="width: var(--room-pct);"></div>
                    </div>
                </div>

                <div class="aspect-item-box">
                    <div style="display: flex; justify-content: space-between; font-size: 0.85rem; font-weight: 700;">
                        <span>🍽️ Dining & Food</span>
                        <span class="mono" style="color: var(--primary);"><%= foodScore %>%</span>
                    </div>
                    <div class="aspect-track">
                        <div class="aspect-fill" style="width: var(--food-pct);"></div>
                    </div>
                </div>

                <div class="aspect-item-box">
                    <div style="display: flex; justify-content: space-between; font-size: 0.85rem; font-weight: 700;">
                        <span>💰 Value for Money</span>
                        <span class="mono" style="color: var(--primary);"><%= valueScore %>%</span>
                    </div>
                    <div class="aspect-track">
                        <div class="aspect-fill" style="width: var(--value-pct);"></div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Verified Reviews Feed -->
        <div class="card">
            <div class="card-header">
                <div>
                    <h2 class="card-title" style="display: flex; align-items: center; gap: 8px;">
                        <span>💬</span> Guest Reviews Audit & Executive Reply Center
                    </h2>
                    <p style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 2px;">
                        Review verified guest comments, inspect AI aspect tags, and publish management responses.
                    </p>
                </div>
                <span class="badge badge-info">Feed: <%= reviews != null ? reviews.size() : 0 %> Reviews</span>
            </div>

            <div style="display: flex; flex-direction: column; gap: var(--space-5);">
                <% if (reviews != null && !reviews.isEmpty()) {
                    for (Review r : reviews) { 
                        String sentimentBadge = "badge-success";
                        if ("NEUTRAL".equalsIgnoreCase(r.getSentiment())) sentimentBadge = "badge-info";
                        else if ("NEGATIVE".equalsIgnoreCase(r.getSentiment())) sentimentBadge = "badge-danger";
                        else if ("CRITICAL".equalsIgnoreCase(r.getSentiment())) sentimentBadge = "badge-danger";
                %>
                    <div class="card" style="background: var(--bg-surface); border-color: var(--border-subtle);">
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: var(--space-3); flex-wrap: wrap; gap: var(--space-3);">
                            <div>
                                <strong style="font-size: 1.05rem; color: var(--text-primary);"><%= r.getGuestName() != null ? r.getGuestName() : "Guest" %></strong>
                                <span style="font-size: 0.85rem; color: var(--text-secondary); margin-left: 8px;">
                                    Stayed in Room <%= r.getRoomNumber() %> (<%= r.getTypeName() %>) • <span class="mono" style="color: var(--primary);">[<%= r.getBookingCode() %>]</span>
                                </span>
                            </div>

                            <div style="display: flex; align-items: center; gap: var(--space-3);">
                                <div style="color: var(--gold); font-size: 1.15rem;">
                                    <% for (int i = 1; i <= 5; i++) { %>
                                        <%= i <= r.getRating() ? "★" : "☆" %>
                                    <% } %>
                                </div>
                                <span class="badge <%= sentimentBadge %>">● <%= r.getSentiment() %></span>
                            </div>
                        </div>

                        <div style="background: var(--bg-canvas); border-left: 3px solid var(--primary); padding: var(--space-3) var(--space-4); border-radius: var(--radius-sm); font-size: 0.92rem; line-height: 1.55; color: var(--text-primary); margin-bottom: var(--space-3);">
                            "<%= r.getReviewText() %>"
                        </div>

                        <!-- Multi-Aspect Pill Badges -->
                        <div style="display: flex; flex-wrap: wrap; gap: var(--space-2); margin-bottom: var(--space-3);">
                            <span class="badge badge-neutral">🧹 Cleanliness: <strong><%= r.getAspectCleanliness() %></strong></span>
                            <span class="badge badge-neutral">🤝 Staff: <strong><%= r.getAspectStaff() %></strong></span>
                            <span class="badge badge-neutral">🛌 Room: <strong><%= r.getAspectRoom() %></strong></span>
                            <span class="badge badge-neutral">🍽️ Food: <strong><%= r.getAspectFood() %></strong></span>
                            <span class="badge badge-neutral">💰 Value: <strong><%= r.getAspectValue() %></strong></span>
                        </div>

                        <% if (r.getKeyHighlights() != null && !r.getKeyHighlights().isEmpty()) { %>
                            <div style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: var(--space-3);">
                                <strong style="color: var(--text-primary);">💡 Extracted Highlights:</strong> <%= r.getKeyHighlights() %>
                            </div>
                        <% } %>

                        <!-- Management Response Section -->
                        <% if (r.getManagerReply() != null && !r.getManagerReply().isEmpty()) { %>
                            <div class="reply-published">
                                <div style="font-weight: 700; font-size: 0.85rem; color: var(--success); margin-bottom: 2px;">
                                    ✓ Published Management Response:
                                </div>
                                <div style="color: var(--text-secondary); font-size: 0.9rem; line-height: 1.5;">
                                    <%= r.getManagerReply() %>
                                </div>
                            </div>
                        <% } else { %>
                            <div class="reply-drawer">
                                <div style="font-weight: 700; font-size: 0.85rem; color: var(--primary); margin-bottom: var(--space-2); display: flex; align-items: center; gap: 6px;">
                                    <span>🤖</span> Google Gemini Drafted Response (Editable):
                                </div>
                                <form action="manager-reviews" method="POST">
                                    <input type="hidden" name="reviewId" value="<%= r.getId() %>">
                                    <textarea name="managerReply" class="form-control" style="min-height: 85px; margin-bottom: var(--space-2);" required><%= r.getAiReplyDraft() != null ? r.getAiReplyDraft() : "" %></textarea>
                                    <button type="submit" class="btn btn-primary btn-sm">
                                        Send Official Reply
                                    </button>
                                </form>
                            </div>
                        <% } %>

                    </div>
                <%  }
                   } else { %>
                    <div style="text-align: center; padding: var(--space-8); color: var(--text-muted);">
                        No guest reviews registered in the system yet.
                    </div>
                <% } %>
            </div>
        </div>

    </main>

</body>
</html>
