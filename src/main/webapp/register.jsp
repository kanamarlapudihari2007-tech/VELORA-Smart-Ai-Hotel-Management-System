<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Guest Account — Velora AI Smart Hotel</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        body {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            padding: var(--space-8) var(--space-4);
            background-color: var(--bg-canvas);
        }
        .reg-container {
            width: 100%;
            max-width: 520px;
        }
        .reg-card {
            background-color: var(--bg-surface);
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-md);
            padding: var(--space-8);
        }
        .reg-header {
            text-align: center;
            margin-bottom: var(--space-6);
        }
        .brand-logo-triangle {
            width: 32px;
            height: 32px;
            margin: 0 auto var(--space-4);
            display: block;
        }
        .reg-title {
            font-size: 1.35rem;
            font-weight: 700;
            color: #ffffff;
            letter-spacing: -0.03em;
        }
        .reg-subtitle {
            font-size: 0.85rem;
            color: var(--text-secondary);
            margin-top: var(--space-1);
        }
        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: var(--space-3);
        }
        .full-col {
            grid-column: 1 / -1;
        }
        .reg-footer {
            text-align: center;
            margin-top: var(--space-6);
            padding-top: var(--space-5);
            border-top: 1px solid var(--border-subtle);
            font-size: 0.825rem;
            color: var(--text-secondary);
        }
        .back-home-link {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            font-size: 0.825rem;
            color: var(--text-muted);
            margin-bottom: var(--space-4);
            transition: color var(--transition-fast);
            text-decoration: none;
        }
        .back-home-link:hover {
            color: #ffffff;
        }
    </style>
</head>
<body>

    <div class="reg-container">
        <a href="login.jsp" class="back-home-link">
            <span>←</span> Back to Sign In
        </a>

        <div class="reg-card">
            <div class="reg-header">
                <img src="images/velora-mark.png" alt="Velora" style="width: 56px; height: 56px; object-fit: contain; margin: 0 auto var(--space-3); display: block; filter: drop-shadow(0 0 16px rgba(212, 175, 55, 0.45));">
                <h1 class="reg-title">Register Guest Account</h1>
                <p class="reg-subtitle">Create your personal guest profile to book luxury suites and access 24/7 AI Concierge</p>
            </div>

            <%
                String errorMessage = (String) request.getAttribute("errorMessage");
                if (errorMessage != null) {
            %>
                <div class="alert alert-danger" style="margin-bottom: var(--space-4);">
                    <span>✕</span>
                    <div><%= errorMessage %></div>
                </div>
            <% } %>

            <form action="register" method="POST">
                <div class="form-grid">
                    <div class="form-group">
                        <label class="form-label" for="username">Username *</label>
                        <input type="text" id="username" name="username" class="form-control" placeholder="Choose username" required autofocus>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="password">Password *</label>
                        <input type="password" id="password" name="password" class="form-control" placeholder="At least 4 chars" required>
                    </div>

                    <div class="form-group full-col">
                        <label class="form-label" for="fullName">Full Legal Name *</label>
                        <input type="text" id="fullName" name="fullName" class="form-control" placeholder="e.g. Alex Johnson" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="email">Email Address *</label>
                        <input type="email" id="email" name="email" class="form-control" placeholder="alex@domain.com" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="phone">Phone Number</label>
                        <input type="text" id="phone" name="phone" class="form-control" placeholder="+91 91234 56789">
                    </div>

                    <div class="form-group full-col">
                        <label class="form-label" for="idProof">Government ID / Passport / Aadhaar</label>
                        <input type="text" id="idProof" name="idProof" class="form-control" placeholder="e.g. PASSPORT-A1234567 or AADHAAR">
                    </div>

                    <div class="form-group full-col">
                        <label class="form-label" for="address">Residential Address</label>
                        <textarea id="address" name="address" class="form-control" style="min-height: 70px;" placeholder="Enter your city, state and country"></textarea>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary btn-block btn-md" style="margin-top: var(--space-4); width: 100%;">
                    Create Account →
                </button>
            </form>

            <div class="reg-footer">
                Already registered with Velora? <a href="login.jsp" style="font-weight: 600; color: #ffffff;">Sign In here</a>
            </div>
        </div>
    </div>

</body>
</html>
