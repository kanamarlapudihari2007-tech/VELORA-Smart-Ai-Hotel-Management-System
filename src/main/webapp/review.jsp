<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hotel.model.Booking" %>
<%@ page import="com.hotel.model.Review" %>
<%@ page import="com.hotel.model.User" %>
<%@ page import="java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect("login.jsp?error=unauthorized");
        return;
    }

    Booking selectedBooking = (Booking) request.getAttribute("selectedBooking");
    Review existingReview = (Review) request.getAttribute("existingReview");
    List<Booking> eligibleBookings = (List<Booking>) request.getAttribute("eligibleBookings");
    List<Review> myPastReviews = (List<Review>) request.getAttribute("myPastReviews");

    String msg = request.getParameter("msg");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Guest Reviews & AI Sentiment — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        .star-rating-bar {
            display: inline-flex;
            flex-direction: row-reverse;
            gap: var(--space-2);
        }
        .star-rating-bar input {
            display: none;
        }
        .star-rating-bar label {
            font-size: 2rem;
            color: var(--text-muted);
            cursor: pointer;
            transition: all var(--transition-fast);
        }
        .star-rating-bar label:hover,
        .star-rating-bar label:hover ~ label,
        .star-rating-bar input:checked ~ label {
            color: #ffffff;
        }
        .aspect-pills-wrap {
            display: flex;
            flex-wrap: wrap;
            gap: var(--space-2);
            margin: var(--space-3) 0;
        }
        .aspect-tag {
            padding: 4px 10px;
            border-radius: var(--radius-full);
            font-size: 0.75rem;
            font-weight: 600;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }
        .tag-positive {
            background: #000000;
            color: var(--success);
            border: 1px solid var(--success-border);
        }
        .tag-neutral {
            background: #000000;
            color: #ededed;
            border: 1px solid var(--border-medium);
        }
        .tag-negative {
            background: #000000;
            color: var(--danger);
            border: 1px solid var(--danger-border);
        }
        .tag-na {
            background: #000000;
            color: var(--text-muted);
            border: 1px solid var(--border-subtle);
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
            <a href="complaint" class="nav-tab-item">Report Issue</a>
            <a href="assistant" class="nav-tab-item">AI Concierge</a>
            <a href="billing" class="nav-tab-item">Billing & Invoices</a>
            <a href="review" class="nav-tab-item active">Reviews</a>
        </nav>
    </header>

    <main class="app-container" style="max-width: 860px;">

        <div class="page-header">
            <div>
                <h1 class="page-title">Guest Review & Multi-Aspect Sentiment Intelligence</h1>
                <p class="page-subtitle">Post feedback on your completed stay. Gemini AI automatically analyzes hospitality aspects and records verified guest sentiments.</p>
            </div>
        </div>

        <!-- Notification Alerts -->
        <% if ("submitted".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <div>
                    <strong>Review Successfully Submitted & Certified!</strong>
                    <div>Google Gemini AI has analyzed your sentiment and drafted an executive response.</div>
                </div>
            </div>
        <% } else if ("already_reviewed".equals(error)) { %>
            <div class="alert alert-info">
                <span>🔒</span>
                <div><strong>Review Already Recorded:</strong> Under hotel authenticity policy, reviews cannot be edited once verified. You can submit another review after your next stay!</div>
            </div>
        <% } else if ("not_utilized".equals(error)) { %>
            <div class="alert alert-danger">
                <span>✕</span>
                <div><strong>Service Not Yet Utilized:</strong> Reviews unlock only after your stay has completed and checkout has occurred.</div>
            </div>
        <% } else if ("missing_fields".equals(error)) { %>
            <div class="alert alert-danger">
                <span>✕</span>
                <div>Please select a star rating and enter your comments before submitting.</div>
            </div>
        <% } %>

        <!-- Completed Stay Selector -->
        <% if (eligibleBookings != null && eligibleBookings.size() > 1) { %>
            <div class="card" style="margin-bottom: var(--space-6); padding: var(--space-3) var(--space-5); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--space-3);">
                <div style="font-weight: 500; color: var(--text-secondary); font-size: 0.85rem;">
                    Select Completed Stay to Review:
                </div>
                <form action="review" method="GET" style="display:flex; align-items:center;">
                    <select name="bookingId" class="form-control" style="width: auto; padding: 4px 10px; font-size: 0.8rem;" onchange="this.form.submit()">
                        <% for (Booking b : eligibleBookings) { 
                            boolean isSelected = (selectedBooking != null && selectedBooking.getId() == b.getId());
                        %>
                            <option value="<%= b.getId() %>" <%= isSelected ? "selected" : "" %>>
                                <%= b.getBookingCode() %> — Room <%= b.getRoomNumber() %> (<%= b.getTypeName() %>)
                            </option>
                        <% } %>
                    </select>
                </form>
            </div>
        <% } %>

        <% if (selectedBooking != null) { %>

            <!-- If Review Already Submitted (Permanent Verified Record) -->
            <% if (existingReview != null) { 
                String sentTagClass = "tag-positive";
                if ("NEUTRAL".equals(existingReview.getSentiment())) sentTagClass = "tag-neutral";
                else if ("NEGATIVE".equals(existingReview.getSentiment()) || "CRITICAL".equals(existingReview.getSentiment())) sentTagClass = "tag-negative";
            %>
                <div class="card" style="margin-bottom: var(--space-8);">
                    <div class="card-header">
                        <div>
                            <h2 class="card-title">
                                Your Review for Room <%= selectedBooking.getRoomNumber() %>
                            </h2>
                            <p style="font-size: 0.8rem; color: var(--text-muted); margin-top: 2px;">
                                Booking Ref: <span class="mono" style="color: #ffffff;"><%= selectedBooking.getBookingCode() %></span> • Stayed <%= selectedBooking.getCheckInDate() %> to <%= selectedBooking.getCheckOutDate() %>
                            </p>
                        </div>
                    </div>

                    <!-- Stars & Quote -->
                    <div style="font-size: 1.5rem; color: #ffffff; margin-bottom: var(--space-3); letter-spacing: 2px;">
                        <% for (int i = 1; i <= 5; i++) { %>
                            <%= i <= existingReview.getRating() ? "★" : "☆" %>
                        <% } %>
                        <span style="font-size: 0.95rem; color: #ededed; font-weight: 600; margin-left: 8px;"><%= existingReview.getRating() %>/5</span>
                    </div>

                    <div style="background: #000000; border-left: 3px solid #ffffff; padding: var(--space-4) var(--space-5); border-radius: 0 var(--radius-sm) var(--radius-sm) 0; font-style: italic; font-size: 0.92rem; line-height: 1.6; color: #ededed; margin-bottom: var(--space-5);">
                        "<%= existingReview.getReviewText() %>"
                    </div>

                    <!-- Gemini AI Sentiment Analysis Box -->
                    <div style="background: #000000; border: 1px solid var(--border-medium); border-radius: var(--radius-sm); padding: var(--space-5); margin-bottom: var(--space-5);">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-3); flex-wrap: wrap; gap: var(--space-2);">
                            <div style="font-size: 0.85rem; font-weight: 600; color: #ffffff;">
                                Google Gemini Multi-Aspect Sentiment Analysis
                            </div>
                            <span class="aspect-tag <%= sentTagClass %>">● <%= existingReview.getSentiment() %></span>
                        </div>

                        <div class="aspect-pills-wrap">
                            <span class="aspect-tag <%= getPillClass(existingReview.getAspectCleanliness()) %>">
                                Cleanliness: <%= existingReview.getAspectCleanliness() %>
                            </span>
                            <span class="aspect-tag <%= getPillClass(existingReview.getAspectStaff()) %>">
                                Staff: <%= existingReview.getAspectStaff() %>
                            </span>
                            <span class="aspect-tag <%= getPillClass(existingReview.getAspectRoom()) %>">
                                Room: <%= existingReview.getAspectRoom() %>
                            </span>
                            <span class="aspect-tag <%= getPillClass(existingReview.getAspectFood()) %>">
                                Dining: <%= existingReview.getAspectFood() %>
                            </span>
                            <span class="aspect-tag <%= getPillClass(existingReview.getAspectValue()) %>">
                                Value: <%= existingReview.getAspectValue() %>
                            </span>
                        </div>

                        <% if (existingReview.getKeyHighlights() != null && !existingReview.getKeyHighlights().isEmpty()) { %>
                            <div style="font-size: 0.82rem; color: var(--text-secondary); background: var(--bg-surface); padding: var(--space-3) var(--space-4); border-radius: var(--radius-sm); border: 1px solid var(--border-subtle); margin-top: var(--space-3);">
                                <strong style="color: #ffffff;">AI Highlights:</strong> <%= existingReview.getKeyHighlights() %>
                            </div>
                        <% } %>
                    </div>

                    <!-- Management Response -->
                    <div style="background: #000000; border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); padding: var(--space-5); margin-bottom: var(--space-5);">
                        <div style="font-weight: 600; color: #ffffff; font-size: 0.85rem; margin-bottom: var(--space-2);">
                            Executive Management Response
                        </div>
                        <div style="color: var(--text-secondary); font-size: 0.85rem; line-height: 1.6;">
                            <%= existingReview.getManagerReply() != null ? existingReview.getManagerReply() : existingReview.getAiReplyDraft() %>
                        </div>
                    </div>

                    <!-- Certified Badge -->
                    <div style="padding: var(--space-4) var(--space-5); background: #000000; border: 1px solid var(--border-medium); border-radius: var(--radius-sm); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--space-3);">
                        <div>
                            <div style="font-weight: 600; color: #ffffff; font-size: 0.85rem;">Verified Stay Review • Immutable Record</div>
                            <div style="color: var(--text-muted); font-size: 0.75rem; margin-top: 2px;">This review is permanently registered. A new review can be posted after completing a subsequent checkout.</div>
                        </div>
                        <span class="badge badge-success">✓ Verified & Certified</span>
                    </div>
                </div>

            <% } else { %>

                <!-- Review Submission Form -->
                <div class="card" style="margin-bottom: var(--space-8);">
                    <div class="card-header">
                        <div>
                            <h2 class="card-title">Share Your Stay Experience</h2>
                            <p style="font-size: 0.8rem; color: var(--text-muted); margin-top: 2px;">
                                Room <%= selectedBooking.getRoomNumber() %> (<%= selectedBooking.getTypeName() %>) • Checked-Out <%= selectedBooking.getCheckOutDate() %>
                            </p>
                        </div>
                    </div>

                    <form action="submit-review" method="POST">
                        <input type="hidden" name="bookingId" value="<%= selectedBooking.getId() %>">

                        <div class="form-group">
                            <label class="form-label">How would you rate your overall stay?</label>
                            <div class="star-rating-bar">
                                <input type="radio" id="star5" name="rating" value="5" required checked><label for="star5" title="5 stars">★</label>
                                <input type="radio" id="star4" name="rating" value="4"><label for="star4" title="4 stars">★</label>
                                <input type="radio" id="star3" name="rating" value="3"><label for="star3" title="3 stars">★</label>
                                <input type="radio" id="star2" name="rating" value="2"><label for="star2" title="2 stars">★</label>
                                <input type="radio" id="star1" name="rating" value="1"><label for="star1" title="1 star">★</label>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="reviewText">Detailed Feedback & Comments:</label>
                            <textarea id="reviewText" name="reviewText" class="form-control" style="min-height: 120px;" required
                                placeholder="Share your experience regarding room hygiene, staff helpfulness, comfort, food, and facilities. Our Gemini AI will analyze your remarks to continuously improve our hospitality..."></textarea>
                        </div>

                        <button type="submit" class="btn btn-primary" style="padding: var(--space-3) var(--space-6); font-weight: 600;">
                            Submit Review →
                        </button>
                    </form>
                </div>

            <% } %>

        <% } else { %>

            <!-- Empty State -->
            <div class="card" style="text-align: center; padding: var(--space-10) var(--space-6);">
                <h2 style="font-size: 1.15rem; color: #ffffff; margin-bottom: var(--space-2);">No Completed Stays to Review</h2>
                <p style="color: var(--text-secondary); max-width: 460px; margin: 0 auto var(--space-6); font-size: 0.85rem; line-height: 1.5;">
                    Guest reviews unlock exclusively <strong>after checkout is completed</strong>. Once your current or upcoming reservation concludes, your review invitation will be available here.
                </p>
                <div style="display: flex; justify-content: center; gap: var(--space-3); flex-wrap: wrap;">
                    <a href="my-bookings" class="btn btn-secondary btn-sm">View My Bookings</a>
                    <a href="search-rooms" class="btn btn-primary btn-sm">Book New Stay →</a>
                </div>
            </div>

        <% } %>

        <!-- Past Reviews History -->
        <% if (myPastReviews != null && !myPastReviews.isEmpty()) { %>
            <div style="margin-top: var(--space-8);">
                <h3 style="font-size: 1rem; font-weight: 600; color: #ffffff; margin-bottom: var(--space-4);">
                    Your Previous Hotel Reviews
                </h3>
                <div style="display: flex; flex-direction: column; gap: var(--space-3);">
                    <% for (Review pr : myPastReviews) { %>
                        <div class="card">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-2);">
                                <div>
                                    <strong style="color: #ffffff;">Room <%= pr.getRoomNumber() %> (<%= pr.getTypeName() %>)</strong>
                                    <span class="mono" style="font-size: 0.8rem; color: var(--text-muted); margin-left: 8px;">[<%= pr.getBookingCode() %>]</span>
                                </div>
                                <div style="color: #ffffff; font-size: 1rem;">
                                    <% for (int i = 1; i <= 5; i++) { %>
                                        <%= i <= pr.getRating() ? "★" : "☆" %>
                                    <% } %>
                                </div>
                            </div>
                            <p style="color: var(--text-secondary); font-size: 0.85rem; font-style: italic; line-height: 1.5; margin-bottom: var(--space-2);">
                                "<%= pr.getReviewText() %>"
                            </p>
                            <div style="display: flex; justify-content: space-between; align-items: center; font-size: 0.75rem; color: var(--text-muted);">
                                <span>AI Sentiment: <strong style="color: #ededed;"><%= pr.getSentiment() %></strong></span>
                                <span class="mono"><%= pr.getCreatedAt() != null ? pr.getCreatedAt().toString().substring(0, 10) : "" %></span>
                            </div>
                        </div>
                    <% } %>
                </div>
            </div>
        <% } %>

    </main>

</body>
</html>
<%!
    private String getPillClass(String aspect) {
        if ("POSITIVE".equalsIgnoreCase(aspect)) return "tag-positive";
        if ("NEUTRAL".equalsIgnoreCase(aspect)) return "tag-neutral";
        if ("NEGATIVE".equalsIgnoreCase(aspect)) return "tag-negative";
        return "tag-na";
    }
%>
