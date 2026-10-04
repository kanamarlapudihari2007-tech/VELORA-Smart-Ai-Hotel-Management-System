<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign In — Velora Smart Hotel Platform</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        :root {
            --gold-accent: #d4af37;
            --gold-glow: rgba(212, 175, 55, 0.35);
        }
        body {
            margin: 0;
            padding: 0;
            min-height: 100vh;
            background: #000000;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: var(--font-sans);
            color: #ededed;
            overflow-x: hidden;
        }

        .login-split-wrapper {
            width: 100%;
            min-height: 100vh;
            display: grid;
            grid-template-columns: 1.15fr 1fr;
            background: #000000;
        }

        /* Left Hero Branding Section */
        .login-hero-pane {
            position: relative;
            background: radial-gradient(circle at 50% 35%, rgba(212, 175, 55, 0.12) 0%, rgba(15, 14, 10, 0.8) 55%, #000000 100%);
            border-right: 1px solid rgba(255, 255, 255, 0.08);
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            padding: 48px 56px;
            overflow: hidden;
        }

        .hero-top-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: rgba(255, 255, 255, 0.04);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 9999px;
            padding: 6px 14px;
            font-size: 0.75rem;
            color: var(--text-secondary);
            font-family: var(--font-mono);
            width: fit-content;
        }

        .hero-top-badge-dot {
            width: 6px;
            height: 6px;
            border-radius: 50%;
            background: #0070f3;
            box-shadow: 0 0 8px #0070f3;
        }

        .hero-brand-center {
            display: flex;
            flex-direction: column;
            align-items: center;
            text-align: center;
            margin: auto 0;
            padding: 24px 0;
        }

        .login-big-logo {
            max-width: 440px;
            width: 90%;
            height: auto;
            object-fit: contain;
            mix-blend-mode: screen;
            filter: drop-shadow(0 0 45px rgba(212, 175, 55, 0.45));
            transition: transform 0.3s ease, filter 0.3s ease;
        }

        .login-big-logo:hover {
            transform: scale(1.02);
            filter: drop-shadow(0 0 60px rgba(212, 175, 55, 0.65));
        }

        .hero-feature-tags {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 12px;
            margin-top: 32px;
            width: 100%;
            max-width: 460px;
        }

        .hero-feature-item {
            background: rgba(255, 255, 255, 0.03);
            border: 1px solid rgba(255, 255, 255, 0.07);
            border-radius: 10px;
            padding: 12px 14px;
            font-size: 0.78rem;
            color: #b0b0b0;
            display: flex;
            align-items: center;
            gap: 8px;
            text-align: left;
        }

        .hero-feature-item svg {
            color: var(--gold-accent);
            flex-shrink: 0;
        }

        .hero-footer-status {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 0.78rem;
            color: var(--text-muted);
            border-top: 1px solid rgba(255, 255, 255, 0.06);
            padding-top: 20px;
        }

        /* Right Form Section */
        .login-form-pane {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            padding: 48px;
            background: #000000;
        }

        .login-card-inner {
            width: 100%;
            max-width: 420px;
        }

        .login-header {
            margin-bottom: 28px;
        }

        .login-header-top {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 20px;
        }

        .back-link {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            font-size: 0.82rem;
            color: var(--text-muted);
            text-decoration: none;
            transition: color 0.15s ease;
        }

        .back-link:hover {
            color: #ffffff;
        }

        .login-form-title {
            font-size: 1.65rem;
            font-weight: 700;
            color: #ffffff;
            letter-spacing: -0.03em;
            margin: 0 0 6px 0;
        }

        .login-form-desc {
            font-size: 0.875rem;
            color: var(--text-secondary);
            margin: 0;
        }

        .input-group-custom {
            position: relative;
            margin-bottom: 18px;
        }

        .input-icon-left {
            position: absolute;
            left: 14px;
            top: 50%;
            transform: translateY(-50%);
            color: var(--text-muted);
            pointer-events: none;
            display: flex;
            align-items: center;
        }

        .form-control-custom {
            width: 100%;
            box-sizing: border-box;
            background: #0c0c0c;
            border: 1px solid rgba(255, 255, 255, 0.12);
            border-radius: 10px;
            padding: 12px 14px 12px 42px;
            font-size: 0.9rem;
            color: #ffffff;
            font-family: var(--font-sans);
            transition: border-color 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
        }

        .form-control-custom:focus {
            outline: none;
            background: #111111;
            border-color: var(--gold-accent);
            box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.2);
        }

        .submit-btn-velora {
            width: 100%;
            background: linear-gradient(180deg, #2a2a2a 0%, #151515 100%);
            border: 1px solid rgba(212, 175, 55, 0.4);
            border-radius: 10px;
            padding: 13px;
            font-size: 0.92rem;
            font-weight: 600;
            color: #ffffff;
            cursor: pointer;
            transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            margin-top: 12px;
            box-shadow: 0 4px 14px rgba(0, 0, 0, 0.6);
        }

        .submit-btn-velora:hover {
            background: linear-gradient(180deg, #333333 0%, #1f1f1f 100%);
            border-color: var(--gold-accent);
            box-shadow: 0 6px 20px rgba(212, 175, 55, 0.25);
            transform: translateY(-1px);
        }

        .submit-btn-velora:active {
            transform: translateY(0);
        }

        .login-footer-card {
            margin-top: 24px;
            padding-top: 20px;
            border-top: 1px solid rgba(255, 255, 255, 0.08);
            text-align: center;
            font-size: 0.84rem;
            color: var(--text-secondary);
        }

        .login-footer-card a {
            color: #ffffff;
            font-weight: 600;
            text-decoration: none;
            margin-left: 4px;
            border-bottom: 1px dotted var(--gold-accent);
        }

        .login-footer-card a:hover {
            color: var(--gold-accent);
        }

        @media (max-width: 900px) {
            .login-split-wrapper {
                grid-template-columns: 1fr;
            }
            .login-hero-pane {
                display: none;
            }
            .login-form-pane {
                padding: 32px 20px;
            }
        }
    </style>
</head>
<body>

    <div class="login-split-wrapper">
        
        <!-- Left Pane: Grand Visual Branding & Logo -->
        <aside class="login-hero-pane">
            <div class="hero-top-badge">
                <span class="hero-top-badge-dot"></span>
                <span>VELORA HOSPITALITY OPERATING SYSTEM</span>
            </div>

            <div class="hero-brand-center">
                <img src="images/velora-logo-transparent.png" 
                     alt="Velora Smart Hotel Management" 
                     class="login-big-logo">

                <div class="hero-feature-tags">
                    <div class="hero-feature-item">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                        </svg>
                        <span>24/7 Gemini AI Concierge</span>
                    </div>
                    <div class="hero-feature-item">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
                        </svg>
                        <span>Dynamic Revenue Pricing</span>
                    </div>
                    <div class="hero-feature-item">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                            <polyline points="14 2 14 8 20 8"/>
                        </svg>
                        <span>Automated Digital Invoicing</span>
                    </div>
                    <div class="hero-feature-item">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <circle cx="12" cy="12" r="10"/>
                            <polyline points="12 6 12 12 16 14"/>
                        </svg>
                        <span>Real-Time Staff Task Dispatch</span>
                    </div>
                </div>
            </div>

            <div class="hero-footer-status">
                <span>Autonomous Hospitality Platform</span>
                <span class="mono" style="color: var(--gold-accent);">Production v2.4</span>
            </div>
        </aside>

        <!-- Right Pane: Authentication Form -->
        <main class="login-form-pane">
            <div class="login-card-inner">
                
                <div class="login-header">
                    <div class="login-header-top">
                        <a href="./" class="back-link">
                            <span>←</span> Back to Velora
                        </a>
                        <img src="images/velora-mark.png" alt="Velora" style="width: 28px; height: 28px; object-fit: contain; mix-blend-mode: screen;">
                    </div>
                    <h1 class="login-form-title">Sign In</h1>
                    <p class="login-form-desc">Enter your account credentials to access your portal</p>
                </div>

                <%
                    String errorMessage = (String) request.getAttribute("errorMessage");
                    String successMessage = (String) session.getAttribute("successMessage");
                    if (successMessage != null) {
                        session.removeAttribute("successMessage");
                    }
                    if ("1".equals(request.getParameter("logout"))) {
                        successMessage = "You have been successfully signed out.";
                    }
                    if ("unauthorized".equals(request.getParameter("error"))) {
                        errorMessage = "Please sign in to access your hotel portal.";
                    }
                %>

                <% if (errorMessage != null) { %>
                    <div class="alert alert-danger" style="margin-bottom: 20px; font-size: 0.85rem;">
                        <span>✕</span>
                        <div><%= errorMessage %></div>
                    </div>
                <% } %>

                <% if (successMessage != null) { %>
                    <div class="alert alert-success" style="margin-bottom: 20px; font-size: 0.85rem;">
                        <span>✓</span>
                        <div><%= successMessage %></div>
                    </div>
                <% } %>

                <form action="login" method="POST">
                    <div class="input-group-custom">
                        <label class="form-label" for="username">Username</label>
                        <div style="position: relative;">
                            <div class="input-icon-left">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                    <circle cx="12" cy="7" r="4"/>
                                </svg>
                            </div>
                            <input type="text" id="username" name="username" class="form-control-custom" placeholder="e.g. guest or your username" required autofocus>
                        </div>
                    </div>

                    <div class="input-group-custom">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-2);">
                            <label class="form-label" for="password" style="margin-bottom: 0;">Password</label>
                        </div>
                        <div style="position: relative;">
                            <div class="input-icon-left">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                                    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                                </svg>
                            </div>
                            <input type="password" id="password" name="password" class="form-control-custom" placeholder="••••••••" required>
                        </div>
                    </div>

                    <button type="submit" class="submit-btn-velora">
                        <span>Sign In to Velora</span>
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <line x1="5" y1="12" x2="19" y2="12"/>
                            <polyline points="12 5 19 12 12 19"/>
                        </svg>
                    </button>
                </form>

                <div class="login-footer-card">
                    New guest to Velora? <a href="register.jsp">Create an Account</a>
                </div>

            </div>
        </main>

    </div>

</body>
</html>
