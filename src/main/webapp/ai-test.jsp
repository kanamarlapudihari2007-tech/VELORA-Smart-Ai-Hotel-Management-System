<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Boolean isConfigured = (Boolean) request.getAttribute("isConfigured");
    if (isConfigured == null) isConfigured = false;
    String maskedKey = (String) request.getAttribute("maskedKey");
    String model = (String) request.getAttribute("model");
    String endpoint = (String) request.getAttribute("endpoint");
    String testResult = (String) request.getAttribute("testResult");
    Long latency = (Long) request.getAttribute("latency");
    String status = (String) request.getAttribute("status");
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gemini AI Integration Diagnostic — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
</head>
<body>

    <header class="app-navbar">
        <a href="dashboard" class="brand-badge">
            <span class="brand-icon">✨</span>
            <span>Velora</span>
            <span class="brand-portal-tag">AI Diagnostics</span>
        </a>
        <div class="nav-actions">
            <a href="dashboard" class="btn btn-secondary btn-sm">← Back to Dashboard</a>
        </div>
    </header>

    <main class="app-container" style="max-width: 860px;">

        <div class="page-header">
            <div>
                <h1 class="page-title">Google Gemini AI Engine Diagnostic Suite</h1>
                <p class="page-subtitle">Verify live connectivity, check API credential bindings, and monitor neural inference response latency.</p>
            </div>
        </div>

        <div class="card" style="margin-bottom: var(--space-6);">
            <div class="card-header">
                <h2 class="card-title">Configuration & Health Check</h2>
                <% if (isConfigured) { %>
                    <span class="badge badge-success">● Gemini API Key Configured</span>
                <% } else { %>
                    <span class="badge badge-warning">○ Gemini API Key Offline / Demo Mode</span>
                <% } %>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: var(--space-4); margin-bottom: var(--space-6);">
                <div style="background: var(--bg-surface); border: 1px solid var(--border-subtle); border-left: 3px solid var(--primary); padding: var(--space-4); border-radius: var(--radius-md);">
                    <div style="font-size: 0.72rem; text-transform: uppercase; color: var(--text-muted); font-weight: 700;">Active Model</div>
                    <div class="mono" style="font-size: 0.95rem; font-weight: 700; color: var(--text-primary); margin-top: 4px;"><%= model != null ? model : "gemini-flash-latest" %></div>
                </div>
                <div style="background: var(--bg-surface); border: 1px solid var(--border-subtle); border-left: 3px solid var(--accent); padding: var(--space-4); border-radius: var(--radius-md);">
                    <div style="font-size: 0.72rem; text-transform: uppercase; color: var(--text-muted); font-weight: 700;">Masked Key</div>
                    <div class="mono" style="font-size: 0.95rem; font-weight: 700; color: var(--text-primary); margin-top: 4px;"><%= maskedKey %></div>
                </div>
                <div style="background: var(--bg-surface); border: 1px solid var(--border-subtle); border-left: 3px solid #6366f1; padding: var(--space-4); border-radius: var(--radius-md);">
                    <div style="font-size: 0.72rem; text-transform: uppercase; color: var(--text-muted); font-weight: 700;">API Endpoint</div>
                    <div class="mono" style="font-size: 0.85rem; font-weight: 700; color: var(--text-secondary); margin-top: 4px; word-break: break-all;"><%= endpoint %></div>
                </div>
            </div>

            <form method="get" action="ai-test">
                <div class="form-group">
                    <label class="form-label">Test Query to Google Gemini Model:</label>
                    <input type="text" name="prompt" class="form-control" value="Hello! What services does this hotel provide?" required>
                </div>
                <button type="submit" class="btn btn-primary">
                    🚀 Dispatch Request to AI
                </button>
            </form>

            <% if ("SUCCESS".equals(status)) { %>
                <div class="alert alert-success" style="margin-top: var(--space-5); display: block;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                        <strong>✓ Google Gemini Live Response Received</strong>
                        <span class="mono" style="font-size: 0.8rem;">Latency: <%= latency %> ms</span>
                    </div>
                    <div style="font-size: 0.92rem; line-height: 1.6; color: var(--text-primary);"><%= testResult %></div>
                </div>
            <% } else if ("FAILED".equals(status)) { %>
                <div class="alert alert-danger" style="margin-top: var(--space-5); display: block;">
                    <strong>✗ Google Gemini Communication Error:</strong>
                    <div style="margin-top: 4px; font-size: 0.88rem;"><%= error %></div>
                </div>
            <% } %>

            <div style="background: var(--bg-surface); border: 1px dashed var(--border-medium); border-radius: var(--radius-md); padding: var(--space-4) var(--space-5); margin-top: var(--space-6); font-size: 0.85rem; color: var(--text-secondary); line-height: 1.6;">
                <h3 style="color: var(--primary); font-size: 0.95rem; margin-bottom: 6px;">💡 API Key Configuration Guidance</h3>
                <p>To supply your personal Google AI Studio Gemini API Key without committing secrets:</p>
                <div class="mono" style="background: var(--bg-canvas); padding: var(--space-3); border-radius: var(--radius-sm); margin: var(--space-2) 0; color: #a5b4fc; font-size: 0.8rem;">
                    $env:GEMINI_API_KEY = "AIzaSyYourActualGeminiKey"<br>
                    # Or place in .env file in the workspace directory (protected by .gitignore)
                </div>
                <p>The backend reads dynamically via <code class="mono">System.getenv("GEMINI_API_KEY")</code>, keeping all credentials completely secure.</p>
            </div>
        </div>

    </main>

</body>
</html>
