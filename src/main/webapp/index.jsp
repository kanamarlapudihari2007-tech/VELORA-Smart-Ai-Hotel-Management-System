<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hotel.model.User" %>
<%
    // Auto-redirect authenticated sessions to their respective dashboard
    User currentUser = (User) session.getAttribute("user");
    if (currentUser != null) {
        if ("MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect("manager-dashboard");
            return;
        } else if ("STAFF".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect("staff-dashboard");
            return;
        } else {
            response.sendRedirect("dashboard");
            return;
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Velora — AI Smart Hotel Management Operating System</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        .hero-banner {
            padding: var(--space-12) 0 var(--space-8);
            text-align: center;
            max-width: 880px;
            margin: 0 auto;
        }
        .hero-badge-pill {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: #111111;
            border: 1px solid var(--border-medium);
            padding: 5px 14px;
            border-radius: var(--radius-full);
            font-size: 0.78rem;
            color: var(--text-secondary);
            font-weight: 500;
            margin-bottom: var(--space-6);
            letter-spacing: 0.02em;
        }
        .hero-badge-dot {
            width: 7px;
            height: 7px;
            border-radius: 50%;
            background-color: var(--success);
            box-shadow: 0 0 8px rgba(16, 185, 129, 0.6);
        }
        .hero-headline {
            font-size: 3.25rem;
            font-weight: 700;
            color: #ffffff;
            letter-spacing: -0.04em;
            line-height: 1.15;
            margin-bottom: var(--space-4);
        }
        .hero-desc {
            font-size: 1.125rem;
            color: var(--text-secondary);
            line-height: 1.6;
            margin-bottom: var(--space-8);
            max-width: 680px;
            margin-left: auto;
            margin-right: auto;
        }
        .hero-actions {
            display: flex;
            gap: var(--space-3);
            justify-content: center;
            flex-wrap: wrap;
            margin-bottom: var(--space-10);
        }
        .telemetry-strip {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
            gap: 1px;
            background: var(--border-subtle);
            border: 1px solid var(--border-subtle);
            border-radius: var(--radius-md);
            overflow: hidden;
            margin-bottom: var(--space-10);
        }
        .telemetry-cell {
            background: var(--bg-surface);
            padding: var(--space-5) var(--space-6);
            text-align: left;
        }
        .telemetry-label {
            font-size: 0.72rem;
            text-transform: uppercase;
            letter-spacing: 0.06em;
            color: var(--text-muted);
            font-weight: 600;
            margin-bottom: var(--space-1);
        }
        .telemetry-value {
            font-family: var(--font-mono);
            font-size: 1.15rem;
            font-weight: 600;
            color: #ffffff;
        }
        .features-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: var(--space-4);
            margin-bottom: var(--space-10);
        }
        .feature-card {
            background: var(--bg-surface);
            border: 1px solid var(--border-subtle);
            border-radius: var(--radius-md);
            padding: var(--space-6);
            transition: border-color var(--transition-fast), background var(--transition-fast);
        }
        .feature-card:hover {
            border-color: var(--border-medium);
            background: var(--bg-card-hover);
        }
        .feature-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: var(--space-3);
        }
        .feature-title {
            font-size: 1.05rem;
            font-weight: 600;
            color: #ffffff;
            letter-spacing: -0.02em;
        }
        .feature-desc {
            font-size: 0.85rem;
            color: var(--text-secondary);
            line-height: 1.5;
        }
        .credentials-card {
            background: var(--bg-surface);
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-md);
            padding: var(--space-6);
            margin-bottom: var(--space-10);
        }
        .credentials-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: var(--space-4);
            margin-top: var(--space-4);
        }
        .cred-item {
            background: #000000;
            border: 1px solid var(--border-subtle);
            border-radius: var(--radius-sm);
            padding: var(--space-3) var(--space-4);
        }
        .cred-role {
            font-size: 0.72rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: var(--text-muted);
            font-weight: 600;
            margin-bottom: 4px;
        }
        .cred-val {
            font-family: var(--font-mono);
            font-size: 0.82rem;
            color: #ededed;
        }
    </style>
</head>
<body>

    <!-- Vercel Dual-Rail Header -->
    <header class="app-navbar">
        <div class="navbar-top">
            <div style="display: flex; align-items: center; gap: var(--space-3);">
                <a href="./" class="brand-badge">
                    <img src="images/velora-mark.png" alt="Velora" class="brand-logo-img" style="mix-blend-mode: screen; width: 28px; height: 28px; object-fit: contain;">
                    <span style="font-weight: 700; letter-spacing: 0.05em; font-size: 0.95rem;">Velora</span>
                </a>
                <span class="brand-separator">/</span>
                <span class="brand-project-switcher">Smart Hotel Management</span>
                <span class="brand-env-badge">Production</span>
            </div>
            <div class="nav-actions">
                <a href="search-rooms" class="btn btn-secondary btn-sm">Explore Suites</a>
                <a href="login.jsp" class="btn btn-primary btn-sm">Sign In →</a>
            </div>
        </div>

        <nav class="navbar-tabs">
            <a href="./" class="nav-tab-item active">Platform Overview</a>
            <a href="search-rooms" class="nav-tab-item">Search & Book</a>
            <a href="login.jsp" class="nav-tab-item">Guest Portal</a>
            <a href="login.jsp" class="nav-tab-item">Operations & Staff</a>
        </nav>
    </header>

    <main class="app-container">

        <!-- Hero Section -->
        <section class="hero-banner">
            <div style="margin-bottom: var(--space-6); display: flex; justify-content: center; align-items: center;">
                <img src="images/velora-logo-transparent.png" 
                     alt="Velora Smart Hotel Management" 
                     style="max-width: 580px; width: 92%; height: auto; object-fit: contain; mix-blend-mode: screen; filter: drop-shadow(0 0 35px rgba(212, 175, 55, 0.45));">
            </div>

            <div class="hero-badge-pill">
                <span class="hero-badge-dot"></span>
                <span>PRODUCTION PLATFORM • VERCEL ARCHITECTURE</span>
            </div>

            <h1 class="hero-headline">
                The intelligent hotel management operating system.
            </h1>

            <p class="hero-desc">
                High-performance autonomous task dispatch, real-time demand-driven dynamic pricing heuristics, multi-aspect guest sentiment tracking, and 24/7 AI guest concierge.
            </p>

            <div class="hero-actions">
                <a href="search-rooms" class="btn btn-primary btn-md">
                    Explore Suites & Bookings →
                </a>
                <a href="login.jsp" class="btn btn-secondary btn-md">
                    Access Portal Console
                </a>
            </div>
        </section>

        <!-- Live Telemetry Strip -->
        <div class="telemetry-strip">
            <div class="telemetry-cell">
                <div class="telemetry-label">Architecture</div>
                <div class="telemetry-value">3-Tier Java EE</div>
            </div>
            <div class="telemetry-cell">
                <div class="telemetry-label">AI Engine</div>
                <div class="telemetry-value">Google Gemini NLP</div>
            </div>
            <div class="telemetry-cell">
                <div class="telemetry-label">Dispatch Latency</div>
                <div class="telemetry-value">&lt; 12ms Task Route</div>
            </div>
            <div class="telemetry-cell">
                <div class="telemetry-label">Security & Integrity</div>
                <div class="telemetry-value">BCrypt • Immutable Reviews</div>
            </div>
        </div>

        <!-- System Capabilities Grid -->
        <div class="features-grid">
            <div class="feature-card">
                <div class="feature-header">
                    <span class="feature-title">Autonomous Task Allocation</span>
                    <span class="badge badge-neutral">Hungarian Matrix</span>
                </div>
                <p class="feature-desc">
                    Real-time workload-balanced task dispatching. Evaluates active staff load, department specialty, and priority queues to distribute maintenance and housekeeping operations.
                </p>
            </div>

            <div class="feature-card">
                <div class="feature-header">
                    <span class="feature-title">Dynamic Pricing Engine</span>
                    <span class="badge badge-neutral">Yield Optimization</span>
                </div>
                <p class="feature-desc">
                    Live revenue management recalculates nightly tariffs using real-time occupancy velocity, day-of-week demand multipliers, and Gemini executive market summaries.
                </p>
            </div>

            <div class="feature-card">
                <div class="feature-header">
                    <span class="feature-title">24/7 AI Hotel Concierge</span>
                    <span class="badge badge-neutral">Gemini Conversational</span>
                </div>
                <p class="feature-desc">
                    Instant natural-language guest query resolution. Surfaces dining hours, room amenities, checkout policies, and local attractions with zero receptionist delay.
                </p>
            </div>

            <div class="feature-card">
                <div class="feature-header">
                    <span class="feature-title">Multi-Aspect Sentiment Tracking</span>
                    <span class="badge badge-neutral">Verified Stays Only</span>
                </div>
                <p class="feature-desc">
                    Tamper-proof review lifecycle strictly unlocked upon checkout. Automatically grades Cleanliness, Staff, Room Quality, Dining, and Value with drafted manager responses.
                </p>
            </div>
        </div>

        <!-- Production Luxury CTA Section -->
        <div class="credentials-card" style="text-align: center; padding: var(--space-8) var(--space-6); background: radial-gradient(circle at 50% 50%, rgba(212, 175, 55, 0.08) 0%, #0a0a0a 100%); border-color: rgba(212, 175, 55, 0.25);">
            <h3 style="font-size: 1.35rem; font-weight: 700; color: #ffffff; margin-bottom: var(--space-2); letter-spacing: -0.02em;">Experience Next-Gen Hospitality</h3>
            <p style="font-size: 0.88rem; color: var(--text-secondary); max-width: 520px; margin: 0 auto var(--space-6); line-height: 1.5;">Discover autonomous hotel operations, real-time demand-driven dynamic pricing, and tailored 24/7 AI guest concierge services.</p>
            <div style="display: flex; justify-content: center; gap: var(--space-3); flex-wrap: wrap;">
                <a href="search-rooms" class="btn btn-primary btn-md">Book Your Stay Now →</a>
                <a href="login.jsp" class="btn btn-secondary btn-md">Sign In to Portal</a>
            </div>
        </div>

    </main>

    <!-- Vercel Minimal Footer -->
    <footer style="border-top: 1px solid var(--border-subtle); padding: var(--space-6) 0; margin-top: var(--space-12);">
        <div class="app-container" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--space-4); font-size: 0.8rem; color: var(--text-muted);">
            <div style="display: flex; align-items: center; gap: var(--space-2);">
                <img src="images/velora-mark.png" alt="Velora" style="width: 20px; height: 20px; object-fit: contain; mix-blend-mode: screen;">
                <span style="font-weight: 500; color: #ffffff;">Velora Smart Hotel Management</span>
            </div>
            <div>Powered by 3-Tier Java EE, MySQL & Google Gemini AI</div>
        </div>
    </footer>

</body>
</html>
