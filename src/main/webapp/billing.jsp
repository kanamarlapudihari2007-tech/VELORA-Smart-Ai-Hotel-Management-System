<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hotel.model.Booking" %>
<%@ page import="com.hotel.model.Payment" %>
<%@ page import="com.hotel.model.ServiceRequest" %>
<%@ page import="com.hotel.model.User" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }

    Booking booking = (Booking) request.getAttribute("booking");
    Payment payment = (Payment) request.getAttribute("payment");
    List<ServiceRequest> serviceRequests = (List<ServiceRequest>) request.getAttribute("serviceRequests");
    List<Booking> availableBookings = (List<Booking>) request.getAttribute("availableBookings");
    Boolean isManagerObj = (Boolean) request.getAttribute("isManager");
    boolean isManager = isManagerObj != null && isManagerObj;

    Long nights = (Long) request.getAttribute("nights");
    if (nights == null) nights = 1L;
    BigDecimal nightlyRate = (BigDecimal) request.getAttribute("nightlyRate");
    BigDecimal baseTariff = (BigDecimal) request.getAttribute("baseTariff");
    BigDecimal taxAmount = (BigDecimal) request.getAttribute("taxAmount");

    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Billing, Invoices & Folio Statement — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        .invoice-card-wrapper {
            background: var(--bg-surface);
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-md);
            padding: var(--space-8);
            margin-bottom: var(--space-8);
        }
        .inv-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            border-bottom: 1px solid var(--border-subtle);
            padding-bottom: var(--space-6);
            margin-bottom: var(--space-6);
            flex-wrap: wrap;
            gap: var(--space-5);
        }
        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: var(--space-4);
            background: #000000;
            border: 1px solid var(--border-subtle);
            border-radius: var(--radius-sm);
            padding: var(--space-5);
            margin-bottom: var(--space-6);
        }
        .info-group-title {
            font-size: 0.7rem;
            text-transform: uppercase;
            color: var(--text-muted);
            font-weight: 600;
            letter-spacing: 0.05em;
            margin-bottom: var(--space-1);
        }
        .info-group-data {
            font-size: 0.9rem;
            font-weight: 600;
            color: #ededed;
        }
        .calc-summary-box {
            max-width: 360px;
            margin-left: auto;
            margin-bottom: var(--space-6);
        }
        .calc-line {
            display: flex;
            justify-content: space-between;
            padding: var(--space-2) 0;
            font-size: 0.85rem;
            color: var(--text-secondary);
        }
        .calc-total-line {
            border-top: 1px solid var(--border-medium);
            padding-top: var(--space-3);
            margin-top: var(--space-2);
            font-size: 1.15rem;
            font-weight: 700;
            color: #ffffff;
        }
        .payment-banner {
            background: #000000;
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-sm);
            padding: var(--space-4) var(--space-5);
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: var(--space-6);
            flex-wrap: wrap;
            gap: var(--space-3);
        }

        /* Print Media Styles */
        @media print {
            body { background: #ffffff !important; color: #000000 !important; }
            .app-navbar, .selector-bar, .inv-actions, .alert { display: none !important; }
            .app-container { max-width: 100% !important; margin: 0 !important; padding: 0 !important; }
            .invoice-card-wrapper {
                background: #ffffff !important;
                color: #000000 !important;
                border: 1px solid #cccccc !important;
                padding: 20px !important;
            }
            .info-grid { background: #f8fafc !important; border: 1px solid #e2e8f0 !important; }
            .info-group-data, .calc-line strong, .calc-total-line { color: #000000 !important; }
            .info-group-title, .calc-line, .table-modern th, .table-modern td { color: #333333 !important; }
            .payment-banner { background: #f0fdf4 !important; border: 1px solid #bbf7d0 !important; color: #166534 !important; }
            .badge { border: 1px solid #000000 !important; color: #000000 !important; }
        }
    </style>
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
                <span class="brand-project-switcher">Billing & Folio</span>
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
                <a href="my-bookings" class="nav-tab-item">Bookings Ledger</a>
                <a href="billing" class="nav-tab-item active">Billing & Invoices</a>
                <a href="dynamic-pricing" class="nav-tab-item">Dynamic Pricing</a>
                <a href="analytics" class="nav-tab-item">Analytics Briefing</a>
                <a href="manager-reviews" class="nav-tab-item">Reviews & Sentiment</a>
            <% } else { %>
                <a href="dashboard" class="nav-tab-item">Overview</a>
                <a href="search-rooms" class="nav-tab-item">Search & Book</a>
                <a href="my-bookings" class="nav-tab-item">My Stays</a>
                <a href="service-request" class="nav-tab-item">Room Service</a>
                <a href="complaint" class="nav-tab-item">Report Issue</a>
                <a href="assistant" class="nav-tab-item">AI Concierge</a>
                <a href="billing" class="nav-tab-item active">Billing & Invoices</a>
                <a href="review" class="nav-tab-item">Reviews</a>
            <% } %>
        </nav>
    </header>

    <main class="app-container" style="max-width: 920px;">

        <% if ("checked_out_success".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>
                    <strong>Checkout Completed Successfully!</strong>
                    <div>Room released to Housekeeping queue. Your finalized official digital tax invoice is shown below.</div>
                </div>
            </div>
        <% } %>

        <!-- Multi-Booking Stay Selector -->
        <% if (availableBookings != null && availableBookings.size() > 1) { %>
            <div class="card selector-bar" style="margin-bottom: var(--space-6); padding: var(--space-3) var(--space-5); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--space-3);">
                <div style="font-size: 0.85rem; font-weight: 500; color: var(--text-secondary);">
                    Select Reservation Statement:
                </div>
                <form action="billing" method="GET" style="display:flex; align-items:center; gap:8px;">
                    <select name="bookingId" class="form-control" style="width: auto; padding: 4px 10px; font-size: 0.8rem;" onchange="this.form.submit()">
                        <% for (Booking b : availableBookings) { 
                            boolean isSelected = (booking != null && booking.getId() == b.getId());
                        %>
                            <option value="<%= b.getId() %>" <%= isSelected ? "selected" : "" %>>
                                <%= b.getBookingCode() %> — Room <%= b.getRoomNumber() %> (<%= b.getStatus() %>) — ₹<%= b.getTotalAmount() %>
                            </option>
                        <% } %>
                    </select>
                </form>
            </div>
        <% } %>

        <% if (booking != null) { 
            String statusBadgeClass = "badge-neutral";
            if ("CHECKED_IN".equals(booking.getStatus())) statusBadgeClass = "badge-success";
            else if ("CHECKED_OUT".equals(booking.getStatus())) statusBadgeClass = "badge-neutral";
        %>

            <!-- Digital Tax Invoice Card -->
            <div class="invoice-card-wrapper" id="printableInvoice">
                
                <div class="inv-header">
                    <div>
                        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
                            <img src="images/velora-mark.png" alt="Velora" style="width: 22px; height: 22px; object-fit: contain;">
                            <h2 style="font-size: 1.25rem; font-weight: 700; color: #ffffff; letter-spacing: -0.02em;">Velora Smart Hotel & Luxury Suites</h2>
                        </div>
                        <p style="color: var(--text-secondary); font-size: 0.8rem;">128 Prime Boulevard, Metro City Center • 24/7 Front Desk Operations</p>
                        <p style="color: var(--text-muted); font-size: 0.75rem; margin-top: 2px;">GSTIN: <strong style="color: var(--text-secondary);" class="mono">27AABCS1429B1Z2</strong> • Official Digital Folio</p>
                    </div>

                    <div style="text-align: right;">
                        <div style="font-size: 1rem; font-weight: 700; color: #ffffff; text-transform: uppercase; letter-spacing: 0.04em;">Digital Tax Invoice</div>
                        <div class="mono" style="font-size: 0.9rem; color: #ededed; margin: 2px 0;">INV-<%= booking.getBookingCode() %></div>
                        <div style="color: var(--text-muted); font-size: 0.75rem;">Issued: <%= booking.getCreatedAt() != null ? booking.getCreatedAt().toString().substring(0, 10) : "Today" %></div>
                        <div style="margin-top: 4px;">
                            <span class="badge <%= statusBadgeClass %>">● <%= booking.getStatus() %></span>
                        </div>
                    </div>
                </div>

                <!-- Info Grid -->
                <div class="info-grid">
                    <div>
                        <div class="info-group-title">Guest Name</div>
                        <div class="info-group-data"><%= booking.getGuestName() != null ? booking.getGuestName() : currentUser.getUsername() %></div>
                    </div>
                    <div>
                        <div class="info-group-title">Room Allocation</div>
                        <div class="info-group-data">Room <%= booking.getRoomNumber() %> (<%= booking.getTypeName() %>)</div>
                    </div>
                    <div>
                        <div class="info-group-title">Check-In Date</div>
                        <div class="info-group-data mono"><%= booking.getCheckInDate() %></div>
                    </div>
                    <div>
                        <div class="info-group-title">Check-Out Date</div>
                        <div class="info-group-data mono"><%= booking.getCheckOutDate() %></div>
                    </div>
                    <div>
                        <div class="info-group-title">Stay Duration</div>
                        <div class="info-group-data"><%= nights %> <%= nights == 1 ? "Night" : "Nights" %></div>
                    </div>
                    <div>
                        <div class="info-group-title">Booking Reference</div>
                        <div class="info-group-data mono" style="color: #ffffff;"><%= booking.getBookingCode() %></div>
                    </div>
                </div>

                <!-- Itemized Breakdown Table -->
                <div class="table-container" style="margin-bottom: var(--space-6);">
                    <table class="table-modern">
                        <thead>
                            <tr>
                                <th>Description</th>
                                <th>Duration</th>
                                <th style="text-align: right;">Nightly Rate</th>
                                <th style="text-align: right;">Amount (₹)</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    <strong style="color: #ffffff;">Room Accommodation</strong>
                                    <div style="font-size: 0.78rem; color: var(--text-muted);"><%= booking.getTypeName() %> — Complimentary High-Speed WiFi, Climate Control & Smart Amenities</div>
                                </td>
                                <td><%= nights %> <%= nights == 1 ? "Night" : "Nights" %></td>
                                <td class="mono" style="text-align: right;">₹<%= nightlyRate %></td>
                                <td class="mono" style="text-align: right; font-weight: 700; color: #ffffff;">₹<%= baseTariff %></td>
                            </tr>

                            <% if (serviceRequests != null && !serviceRequests.isEmpty()) { 
                                for (ServiceRequest sr : serviceRequests) { %>
                                    <tr>
                                        <td>
                                            <strong style="color: #ededed;">Room Service: <%= sr.getRequestType() %></strong>
                                            <div style="font-size: 0.78rem; color: var(--text-muted);">
                                                <%= sr.getExtractedItems() != null ? sr.getExtractedItems() : sr.getRawText() %>
                                            </div>
                                        </td>
                                        <td>1 Request</td>
                                        <td style="text-align: right; color: var(--text-muted);">Complimentary</td>
                                        <td class="mono" style="text-align: right; color: var(--text-muted);">₹0.00</td>
                                    </tr>
                            <%  } 
                               } %>
                        </tbody>
                    </table>
                </div>

                <!-- Calculation Summary -->
                <div class="calc-summary-box">
                    <div class="calc-line">
                        <span>Room Subtotal:</span>
                        <strong class="mono" style="color: #ededed;">₹<%= baseTariff %></strong>
                    </div>
                    <div class="calc-line">
                        <span>Hotel GST & Service Tax (12%):</span>
                        <strong class="mono" style="color: var(--text-secondary);">₹<%= taxAmount %> (Included)</strong>
                    </div>
                    <div class="calc-line calc-total-line">
                        <span>Net Total Settled:</span>
                        <span class="mono">₹<%= booking.getTotalAmount() %></span>
                    </div>
                </div>

                <!-- Payment Settlement Box -->
                <div class="payment-banner">
                    <div>
                        <div style="font-weight: 600; color: #ffffff; font-size: 0.9rem;">Payment Status: <%= payment != null ? payment.getPaymentStatus() : "COMPLETED" %></div>
                        <div style="color: var(--text-secondary); font-size: 0.78rem; margin-top: 2px;">
                            Method: <strong><%= payment != null && payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "ONLINE / CARD" %></strong>
                            • Settled on: <%= payment != null && payment.getPaymentDate() != null ? payment.getPaymentDate().toString().substring(0, 16) : booking.getCreatedAt() %>
                        </div>
                    </div>
                    <span class="badge badge-success">PAID IN FULL</span>
                </div>

                <!-- Action Controls -->
                <div class="inv-actions" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--space-3); padding-top: var(--space-4); border-top: 1px solid var(--border-subtle);">
                    <button type="button" onclick="window.print()" class="btn btn-secondary btn-sm">
                        Print / Save PDF Invoice
                    </button>

                    <div style="display: flex; gap: var(--space-2); align-items: center;">
                        <% if ("CHECKED_IN".equals(booking.getStatus())) { %>
                            <form action="checkout" method="POST" style="margin: 0;">
                                <input type="hidden" name="bookingId" value="<%= booking.getId() %>">
                                <button type="submit" class="btn btn-primary btn-sm">
                                    Settle Bill & Checkout →
                                </button>
                            </form>
                        <% } else if ("CHECKED_OUT".equals(booking.getStatus())) { %>
                            <a href="review?bookingId=<%= booking.getId() %>" class="btn btn-primary btn-sm">
                                Rate & Review Stay →
                            </a>
                        <% } else if ("RESERVED".equals(booking.getStatus())) { %>
                            <a href="checkin?bookingId=<%= booking.getId() %>" class="btn btn-primary btn-sm">
                                Proceed to Check-In →
                            </a>
                        <% } %>

                        <a href="my-bookings" class="btn btn-secondary btn-sm">Back to Bookings</a>
                    </div>
                </div>

            </div>

        <% } else { %>
            <div class="card" style="text-align: center; padding: var(--space-10) var(--space-6);">
                <h3 style="font-size: 1.15rem; font-weight: 600; color: #ffffff; margin-bottom: var(--space-1);">No Reservation or Billing Record Found</h3>
                <p style="color: var(--text-secondary); max-width: 450px; margin: 0 auto var(--space-6); font-size: 0.85rem;">
                    You do not currently have any active or past booking statements associated with this account.
                </p>
                <a href="search-rooms" class="btn btn-primary btn-sm">
                    Browse Available Rooms →
                </a>
            </div>
        <% } %>

    </main>

</body>
</html>
