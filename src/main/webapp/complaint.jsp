<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.hotel.model.Booking" %>
<%@ page import="com.hotel.model.Complaint" %>
<%@ page import="com.hotel.model.ComplaintClassificationResult" %>
<%@ page import="com.hotel.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }

    Booking activeBooking = (Booking) request.getAttribute("activeBooking");
    List<Complaint> complaints = (List<Complaint>) request.getAttribute("complaints");
    ComplaintClassificationResult aiResult = (ComplaintClassificationResult) request.getAttribute("aiClassification");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Report Issue & AI Assistance — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        .complaint-layout {
            display: grid;
            grid-template-columns: 360px 1fr;
            gap: var(--space-6);
            align-items: start;
        }
        @media (max-width: 950px) {
            .complaint-layout {
                grid-template-columns: 1fr;
            }
        }
        .ai-banner-card {
            background: #000000;
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-sm);
            padding: var(--space-5);
            margin-bottom: var(--space-6);
        }
        .active-stay-pill {
            background: #000000;
            border: 1px solid var(--border-subtle);
            border-radius: var(--radius-sm);
            padding: var(--space-3) var(--space-4);
            margin-bottom: var(--space-4);
            font-size: 0.85rem;
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
                <span class="brand-project-switcher">Guest Portal</span>
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
            <a href="dashboard" class="nav-tab-item">Overview</a>
            <a href="search-rooms" class="nav-tab-item">Search & Book</a>
            <a href="my-bookings" class="nav-tab-item">My Stays</a>
            <a href="service-request" class="nav-tab-item">Room Service</a>
            <a href="complaint" class="nav-tab-item active">Report Issue</a>
            <a href="assistant" class="nav-tab-item">AI Concierge</a>
            <a href="billing" class="nav-tab-item">Billing & Invoices</a>
            <a href="review" class="nav-tab-item">Reviews</a>
        </nav>
    </header>

    <main class="app-container">

        <div class="page-header">
            <div>
                <h1 class="page-title">Issue Reporting & Rapid Staff Escalation</h1>
                <p class="page-subtitle">Report any inconvenience or room maintenance requirement. Gemini AI evaluates severity and immediately notifies the responsible department.</p>
            </div>
        </div>

        <% if (errorMessage != null) { %>
            <div class="alert alert-danger">
                <span>✕</span>
                <div><%= errorMessage %></div>
            </div>
        <% } %>

        <% if (aiResult != null) { %>
            <div class="ai-banner-card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-3); flex-wrap: wrap; gap: var(--space-2);">
                    <strong style="color: #ffffff; font-size: 0.95rem;">
                        <%= aiResult.isClassifiedByAI() ? "Google Gemini AI Analysis Applied" : "Smart Rule Analysis Applied" %>
                    </strong>
                    <span class="badge badge-neutral">Sector: <%= aiResult.getCategory() %></span>
                </div>
                <div style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: var(--space-3);">
                    <span>Assigned Priority:</span> <strong style="color: #ffffff;"><%= aiResult.getPriority() %></strong> &nbsp;|&nbsp;
                    <span>Ticket Summary:</span> <strong style="color: #ededed;"><%= aiResult.getShortDescription() %></strong>
                </div>
                <div style="background: rgba(16, 185, 129, 0.08); border-left: 2px solid var(--success); padding: var(--space-3) var(--space-4); border-radius: var(--radius-sm); font-size: 0.85rem; color: #a7f3d0;">
                    <em><%= aiResult.getReassuranceMessage() %></em>
                </div>
            </div>
        <% } else if ("submitted".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Issue registered successfully! Attending department dispatched.</div>
            </div>
        <% } %>

        <div class="complaint-layout">
            <!-- Submit Complaint Form Card -->
            <div class="card">
                <div class="card-header">
                    <h2 class="card-title">Log an Issue / Complaint</h2>
                </div>

                <% if (activeBooking != null) { %>
                    <div class="active-stay-pill">
                        <div>Active Stay: <strong>Room <%= activeBooking.getRoomNumber() %></strong></div>
                        <div style="color: var(--text-muted); font-size: 0.78rem; margin-top: 2px;">
                            Booking Ref: <span class="mono" style="color: #ffffff;"><%= activeBooking.getBookingCode() %></span>
                        </div>
                    </div>

                    <form action="complaint" method="POST">
                        <div class="form-group">
                            <label for="rawText" class="form-label">Describe Problem in Natural Language *</label>
                            <textarea id="rawText" name="rawText" class="form-control" style="min-height: 80px;" placeholder="e.g. The AC in room 201 is making a rattling noise, please check it." required></textarea>
                        </div>

                        <div class="form-group">
                            <label for="category" class="form-label">Issue Sector / Category</label>
                            <select id="category" name="category" class="form-control">
                                <option value="AUTO" selected>Auto-Detect with Gemini AI</option>
                                <option value="Maintenance">Maintenance (AC, Plumbing, Electrical)</option>
                                <option value="Housekeeping">Housekeeping & Hygiene</option>
                                <option value="Food Service">Food Service / Dining</option>
                                <option value="Security">Noise Disturbance / Security</option>
                                <option value="Reception">Front Desk / Keycard / Billing</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="priority" class="form-label">Urgency / Priority</label>
                            <select id="priority" name="priority" class="form-control">
                                <option value="AUTO" selected>Auto-Detect with Gemini AI</option>
                                <option value="URGENT">URGENT</option>
                                <option value="HIGH">HIGH</option>
                                <option value="NORMAL">NORMAL</option>
                                <option value="LOW">LOW</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="shortDescription" class="form-label">Custom Summary <span style="color: var(--text-muted); font-weight: 400;">(Optional)</span></label>
                            <input type="text" id="shortDescription" name="shortDescription" class="form-control" placeholder="Leave blank for AI summary">
                        </div>

                        <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: var(--space-2);">
                            Submit Issue →
                        </button>
                    </form>
                <% } else { %>
                    <div class="alert alert-danger" style="margin-bottom: 0;">
                        <span>✕</span>
                        <div>Active checked-in reservation required to log room issues. <a href="search-rooms" style="color: #ffffff; text-decoration: underline;">Book a room</a>.</div>
                    </div>
                <% } %>
            </div>

            <!-- Complaints History Table Card -->
            <div class="card" style="padding: 0; overflow: hidden;">
                <div style="padding: var(--space-4) var(--space-6); border-bottom: 1px solid var(--border-subtle);">
                    <div style="font-size: 0.95rem; font-weight: 600; color: #ffffff;">
                        Reported Complaints History
                    </div>
                </div>

                <div class="table-container">
                    <table class="table-modern">
                        <thead>
                            <tr>
                                <th>Ref</th>
                                <th>Room</th>
                                <th>Category</th>
                                <th>Short Description</th>
                                <th>Priority</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (complaints != null && !complaints.isEmpty()) {
                                for (Complaint c : complaints) {
                                    String badgeClass = "badge-neutral";
                                    if ("IN_PROGRESS".equals(c.getStatus())) badgeClass = "badge-warning";
                                    else if ("RESOLVED".equals(c.getStatus())) badgeClass = "badge-success";

                                    String prioClass = "badge-neutral";
                                    if ("HIGH".equals(c.getPriority()) || "URGENT".equals(c.getPriority())) prioClass = "badge-danger";
                            %>
                                <tr>
                                    <td><span class="mono" style="font-weight: 600; color: #ffffff;">#CMP-<%= c.getId() %></span></td>
                                    <td>Room <%= c.getRoomNumber() %></td>
                                    <td><span class="badge badge-neutral"><%= c.getCategory() %></span></td>
                                    <td style="color: #ededed; font-weight: 500;"><%= c.getShortDescription() %></td>
                                    <td><span class="badge <%= prioClass %>"><%= c.getPriority() %></span></td>
                                    <td><span class="badge <%= badgeClass %>"><%= c.getStatus() %></span></td>
                                </tr>
                            <%  }
                               } else { %>
                                <tr>
                                    <td colspan="6" style="text-align: center; color: var(--text-muted); padding: var(--space-8);">No complaints reported yet.</td>
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
