<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.hotel.model.Booking" %>
<%@ page import="com.hotel.model.ServiceRequest" %>
<%@ page import="com.hotel.model.ServiceRequestAIResult" %>
<%@ page import="com.hotel.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }

    Booking activeBooking = (Booking) request.getAttribute("activeBooking");
    List<ServiceRequest> requests = (List<ServiceRequest>) request.getAttribute("requests");
    ServiceRequestAIResult aiResult = (ServiceRequestAIResult) request.getAttribute("aiResult");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Room Service Requests — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        .service-layout {
            display: grid;
            grid-template-columns: 360px 1fr;
            gap: var(--space-6);
            align-items: start;
        }
        @media (max-width: 950px) {
            .service-layout {
                grid-template-columns: 1fr;
            }
        }
        .ai-intelligence-card {
            background: #000000;
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-sm);
            padding: var(--space-5);
            margin-bottom: var(--space-6);
        }
        .stay-active-badge {
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
            <a href="service-request" class="nav-tab-item active">Room Service</a>
            <a href="complaint" class="nav-tab-item">Report Issue</a>
            <a href="assistant" class="nav-tab-item">AI Concierge</a>
            <a href="billing" class="nav-tab-item">Billing & Invoices</a>
            <a href="review" class="nav-tab-item">Reviews</a>
        </nav>
    </header>

    <main class="app-container">

        <div class="page-header">
            <div>
                <h1 class="page-title">Natural Language Room Service & Amenities</h1>
                <p class="page-subtitle">Describe your requests naturally. Gemini AI will automatically extract items, prioritize urgency, and route directly to staff.</p>
            </div>
        </div>

        <% if (errorMessage != null) { %>
            <div class="alert alert-danger">
                <span>✕</span>
                <div><%= errorMessage %></div>
            </div>
        <% } %>

        <% if (aiResult != null) { %>
            <div class="ai-intelligence-card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-3); flex-wrap: wrap; gap: var(--space-2);">
                    <strong style="color: #ffffff; font-size: 0.95rem;">
                        <%= aiResult.isClassifiedByAI() ? "Google Gemini AI Smart Extraction Applied" : "Smart Rule Extraction Applied" %>
                    </strong>
                    <div style="display: flex; gap: var(--space-2);">
                        <span class="badge badge-neutral">Dept: <%= aiResult.getRequestType() %></span>
                        <span class="badge badge-neutral">ETA: <%= aiResult.getEstimatedMinutes() %>m</span>
                        <span class="badge badge-neutral">Priority: <%= aiResult.getPriority() %></span>
                    </div>
                </div>

                <div style="background: var(--bg-surface); padding: var(--space-3) var(--space-4); border-radius: var(--radius-sm); margin-bottom: var(--space-3); border: 1px solid var(--border-subtle);">
                    <div style="font-size: 0.7rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 2px;">
                        Structured Extracted Items
                    </div>
                    <div style="color: #ffffff; font-weight: 600; font-size: 0.9rem;">
                        <%= aiResult.getExtractedItems() %>
                    </div>
                </div>

                <div style="background: rgba(16, 185, 129, 0.08); border-left: 2px solid var(--success); padding: var(--space-3) var(--space-4); border-radius: var(--radius-sm); font-size: 0.85rem; color: #a7f3d0;">
                    <em><%= aiResult.getConfirmationMessage() %></em>
                </div>
            </div>
        <% } else if ("submitted".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>Service request submitted successfully! Attending department dispatched.</div>
            </div>
        <% } %>

        <div class="service-layout">
            <!-- Submit Request Form Card -->
            <div class="card">
                <div class="card-header">
                    <h2 class="card-title">Order Room Service</h2>
                </div>

                <% if (activeBooking != null) { %>
                    <div class="stay-active-badge">
                        <div>Active Stay: <strong>Room <%= activeBooking.getRoomNumber() %></strong></div>
                        <div style="color: var(--text-muted); font-size: 0.78rem; margin-top: 2px;">
                            Booking Ref: <span class="mono" style="color: #ffffff;"><%= activeBooking.getBookingCode() %></span>
                        </div>
                    </div>

                    <form action="service-request" method="POST">
                        <div class="form-group">
                            <label for="rawText" class="form-label">Natural Language Request *</label>
                            <textarea id="rawText" name="rawText" class="form-control" style="min-height: 80px;" placeholder="e.g. Please bring 2 extra bath towels and 1 bottle of mineral water." required></textarea>
                        </div>

                        <div class="form-group">
                            <label for="requestType" class="form-label">Department / Category</label>
                            <select id="requestType" name="requestType" class="form-control">
                                <option value="AUTO" selected>Auto-Detect with Gemini AI</option>
                                <option value="Housekeeping">Housekeeping</option>
                                <option value="Food Service">Food & Beverage Room Service</option>
                                <option value="Maintenance">Technical / Electrical Amenities</option>
                                <option value="Reception">Front Desk / Concierge</option>
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
                            <label for="extractedItems" class="form-label">Custom Summary <span style="color: var(--text-muted); font-weight: 400;">(Optional)</span></label>
                            <input type="text" id="extractedItems" name="extractedItems" class="form-control" placeholder="Leave blank for automatic Gemini extraction">
                        </div>

                        <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: var(--space-2);">
                            Submit Request →
                        </button>
                    </form>
                <% } else { %>
                    <div class="alert alert-danger" style="margin-bottom: 0;">
                        <span>✕</span>
                        <div>Active checked-in reservation required to order room service. <a href="search-rooms" style="color: #ffffff; text-decoration: underline;">Book a room</a>.</div>
                    </div>
                <% } %>
            </div>

            <!-- Service Requests History Table Card -->
            <div class="card" style="padding: 0; overflow: hidden;">
                <div style="padding: var(--space-4) var(--space-6); border-bottom: 1px solid var(--border-subtle);">
                    <div style="font-size: 0.95rem; font-weight: 600; color: #ffffff;">
                        Service Requests History
                    </div>
                </div>

                <div class="table-container">
                    <table class="table-modern">
                        <thead>
                            <tr>
                                <th>Ref</th>
                                <th>Room</th>
                                <th>Category</th>
                                <th>AI-Extracted Items</th>
                                <th>Priority</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (requests != null && !requests.isEmpty()) {
                                for (ServiceRequest sr : requests) {
                                    String badgeClass = "badge-neutral";
                                    if ("IN_PROGRESS".equals(sr.getStatus())) badgeClass = "badge-warning";
                                    else if ("COMPLETED".equals(sr.getStatus())) badgeClass = "badge-success";

                                    String prioClass = "badge-neutral";
                                    if ("URGENT".equals(sr.getPriority())) prioClass = "badge-danger";
                                    else if ("HIGH".equals(sr.getPriority())) prioClass = "badge-warning";
                            %>
                                <tr>
                                    <td><span class="mono" style="font-weight: 600; color: #ffffff;">#SR-<%= sr.getId() %></span></td>
                                    <td>Room <%= sr.getRoomNumber() %></td>
                                    <td><span class="badge badge-neutral"><%= sr.getRequestType() %></span></td>
                                    <td style="color: #ededed; font-weight: 500;"><%= sr.getExtractedItems() != null ? sr.getExtractedItems() : sr.getRawText() %></td>
                                    <td><span class="badge <%= prioClass %>"><%= sr.getPriority() %></span></td>
                                    <td><span class="badge <%= badgeClass %>"><%= sr.getStatus() %></span></td>
                                </tr>
                            <%  }
                               } else { %>
                                <tr>
                                    <td colspan="6" style="text-align: center; color: var(--text-muted); padding: var(--space-8);">No service requests submitted yet.</td>
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
