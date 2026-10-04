<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.hotel.model.ChatMessage" %>
<%@ page import="com.hotel.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    List<ChatMessage> chatHistory = (List<ChatMessage>) request.getAttribute("chatHistory");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Velora AI — 24/7 Intelligent Concierge — Velora</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&family=Geist+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/design-system.css">
    <style>
        body {
            height: 100vh;
            overflow: hidden;
            display: flex;
            flex-direction: column;
        }
        .chat-app-wrapper {
            max-width: 920px;
            width: 100%;
            margin: var(--space-4) auto;
            padding: 0 var(--space-4);
            flex: 1;
            display: flex;
            flex-direction: column;
            min-height: 0;
        }
        .chat-frame-card {
            background: var(--bg-surface);
            border: 1px solid var(--border-medium);
            border-radius: var(--radius-md);
            display: flex;
            flex-direction: column;
            height: calc(100vh - 120px);
            max-height: 820px;
            min-height: 480px;
            overflow: hidden;
        }
        .chat-frame-header {
            background: #000000;
            padding: var(--space-4) var(--space-6);
            border-bottom: 1px solid var(--border-subtle);
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-shrink: 0;
        }
        .assistant-avatar {
            width: 36px;
            height: 36px;
            border-radius: var(--radius-sm);
            background: #111111;
            border: 1px solid var(--border-medium);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1rem;
            color: #ffffff;
            flex-shrink: 0;
        }
        .messages-scroll-area {
            flex: 1 1 0%;
            min-height: 0;
            overflow-y: auto;
            padding: var(--space-6);
            display: flex;
            flex-direction: column;
            gap: var(--space-3);
            scroll-behavior: smooth;
        }
        .chat-msg-row {
            display: flex;
            gap: var(--space-3);
            max-width: 80%;
        }
        .chat-msg-row.user {
            align-self: flex-end;
            flex-direction: row-reverse;
        }
        .chat-msg-row.assistant {
            align-self: flex-start;
        }
        .chat-bubble {
            padding: var(--space-3) var(--space-4);
            border-radius: var(--radius-md);
            font-size: 0.88rem;
            line-height: 1.5;
            word-wrap: break-word;
        }
        .chat-msg-row.user .chat-bubble {
            background: #ffffff;
            color: #000000;
            border-bottom-right-radius: 2px;
            font-weight: 500;
        }
        .chat-msg-row.assistant .chat-bubble {
            background: #000000;
            color: #ededed;
            border: 1px solid var(--border-subtle);
            border-bottom-left-radius: 2px;
        }
        .chat-bubble-footer {
            font-size: 0.7rem;
            color: var(--text-muted);
            margin-top: var(--space-1);
            display: flex;
            gap: var(--space-2);
            align-items: center;
        }
        .chat-msg-row.user .chat-bubble-footer {
            justify-content: flex-end;
            color: #555555;
        }
        .chips-bar {
            flex-shrink: 0;
            padding: var(--space-2) var(--space-5);
            background: #000000;
            border-top: 1px solid var(--border-subtle);
            display: flex;
            gap: var(--space-2);
            flex-wrap: wrap;
            align-items: center;
        }
        .chip-button {
            background: var(--bg-surface);
            border: 1px solid var(--border-subtle);
            color: var(--text-secondary);
            padding: 3px 10px;
            border-radius: var(--radius-full);
            font-size: 0.75rem;
            font-weight: 500;
            cursor: pointer;
            transition: all var(--transition-fast);
        }
        .chip-button:hover {
            background: var(--bg-card-hover);
            color: #ffffff;
            border-color: var(--border-medium);
        }
        .chat-composer-bar {
            flex-shrink: 0;
            background: #000000;
            padding: var(--space-3) var(--space-5);
            border-top: 1px solid var(--border-medium);
        }
        .composer-form {
            display: flex;
            gap: var(--space-2);
            width: 100%;
        }
        .typing-pill {
            display: none;
            align-items: center;
            gap: 4px;
            padding: 6px 12px;
            background: #000000;
            border-radius: var(--radius-full);
            width: fit-content;
            border: 1px solid var(--border-subtle);
            margin-bottom: var(--space-2);
        }
        .typing-dot {
            width: 5px;
            height: 5px;
            background: var(--text-muted);
            border-radius: 50%;
            animation: pulse 1.4s infinite ease-in-out;
        }
        .typing-dot:nth-child(2) { animation-delay: 0.2s; }
        .typing-dot:nth-child(3) { animation-delay: 0.4s; }
        @keyframes pulse {
            0%, 80%, 100% { opacity: 0.2; transform: scale(0.8); }
            40% { opacity: 1; transform: scale(1.2); }
        }
    </style>
</head>
<body>

    <!-- Vercel Dual-Rail Header -->
    <header class="app-navbar">
        <div class="navbar-top">
            <div style="display: flex; align-items: center; gap: var(--space-3);">
                <a href="${pageContext.request.contextPath}/dashboard" class="brand-badge">
                    <img src="images/velora-mark.png" alt="Velora" class="brand-logo-img">
                    <span>Velora</span>
                </a>
                <span class="brand-separator">/</span>
                <span class="brand-project-switcher">AI Concierge</span>
                <span class="brand-env-badge">Production</span>
            </div>
            <div class="nav-actions">
                <a href="${pageContext.request.contextPath}/assistant?action=clear" class="btn btn-secondary btn-sm" onclick="return confirm('Clear your chat history session?');">Clear History</a>
                <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary btn-sm">← Back</a>
            </div>
        </div>

        <nav class="navbar-tabs">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-tab-item">Overview</a>
            <a href="${pageContext.request.contextPath}/search-rooms" class="nav-tab-item">Search & Book</a>
            <a href="${pageContext.request.contextPath}/my-bookings" class="nav-tab-item">My Stays</a>
            <a href="${pageContext.request.contextPath}/service-request" class="nav-tab-item">Room Service</a>
            <a href="${pageContext.request.contextPath}/complaint" class="nav-tab-item">Report Issue</a>
            <a href="${pageContext.request.contextPath}/assistant" class="nav-tab-item active">AI Concierge</a>
            <a href="${pageContext.request.contextPath}/billing" class="nav-tab-item">Billing & Invoices</a>
            <a href="${pageContext.request.contextPath}/review" class="nav-tab-item">Reviews</a>
        </nav>
    </header>

    <div class="chat-app-wrapper">
        <div class="chat-frame-card">
            
            <!-- Chat Card Header -->
            <div class="chat-frame-header">
                <div style="display: flex; align-items: center; gap: var(--space-3);">
                    <div class="assistant-avatar">
                        <svg style="width: 18px; height: 18px;" viewBox="0 0 24 24" fill="none" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M12 2a2 2 0 0 1 2 2v2a2 2 0 0 1-2 2 2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z"/>
                            <rect x="4" y="8" width="16" height="12" rx="2"/>
                            <circle cx="9" cy="14" r="1"/>
                            <circle cx="15" cy="14" r="1"/>
                        </svg>
                    </div>
                    <div>
                        <div style="font-weight: 600; font-size: 0.95rem; color: #ffffff;">Velora AI — Intelligent Hotel Concierge</div>
                        <div style="font-size: 0.75rem; color: var(--text-muted); display: flex; align-items: center; gap: 6px;">
                            <span class="user-status-dot"></span> Powered by Google Gemini AI & Live Database
                        </div>
                    </div>
                </div>
            </div>

            <!-- Messages Thread Area -->
            <div class="messages-scroll-area" id="messagesContainer">
                <% if (chatHistory != null) {
                    for (ChatMessage msg : chatHistory) {
                        boolean isUser = "user".equalsIgnoreCase(msg.getSender());
                %>
                    <div class="chat-msg-row <%= isUser ? "user" : "assistant" %>">
                        <div class="chat-bubble">
                            <%= msg.getMessage().replace("\n", "<br>") %>
                            <div class="chat-bubble-footer">
                                <span class="mono"><%= msg.getTimestamp() %></span>
                                <% if (!isUser) { %>
                                    <span>• <%= msg.isFromAI() ? "Gemini AI" : "Concierge Guide" %></span>
                                <% } %>
                            </div>
                        </div>
                    </div>
                <%  }
                   } %>

                <!-- Typing indicator -->
                <div class="typing-pill" id="typingIndicator">
                    <span class="typing-dot"></span>
                    <span class="typing-dot"></span>
                    <span class="typing-dot"></span>
                    <span style="font-size: 0.72rem; color: var(--text-muted); margin-left: 6px;">Velora AI is processing...</span>
                </div>
            </div>

            <!-- Quick Suggestion Chips -->
            <div class="chips-bar">
                <span style="font-size: 0.7rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600; letter-spacing: 0.05em;">Suggested:</span>
                <button type="button" class="chip-button" onclick="askSuggestion('What are the breakfast and dining timings?')">Breakfast Timings</button>
                <button type="button" class="chip-button" onclick="askSuggestion('What is my current room and booking status?')">My Booking</button>
                <button type="button" class="chip-button" onclick="askSuggestion('Can I get two extra towels and a dental kit sent to my room?')">Extra Towels</button>
                <button type="button" class="chip-button" onclick="askSuggestion('What is the Wi-Fi network and password?')">Wi-Fi Details</button>
                <button type="button" class="chip-button" onclick="askSuggestion('Where is the swimming pool and gym located, and what are their hours?')">Pool & Gym</button>
                <button type="button" class="chip-button" onclick="askSuggestion('What are the check-in and late check-out policies?')">Late Check-out</button>
                <button type="button" class="chip-button" onclick="askSuggestion('How do I contact the Front Desk or request housekeeping?')">Front Desk</button>
            </div>

            <!-- Input Bar -->
            <div class="chat-composer-bar">
                <form id="chatForm" class="composer-form" action="${pageContext.request.contextPath}/assistant" method="POST" onsubmit="handleFormSubmit(event);">
                    <input type="text" id="userInput" name="message" class="form-control" placeholder="Ask Velora AI about amenities, dining hours, or your reservation..." autocomplete="off" required>
                    <button type="submit" class="btn btn-primary" id="sendBtn" style="flex-shrink: 0; min-width: 80px;">
                        <span>Send</span> →
                    </button>
                </form>
            </div>

        </div>
    </div>

    <script>
        const messagesContainer = document.getElementById('messagesContainer');
        const userInput = document.getElementById('userInput');
        const typingIndicator = document.getElementById('typingIndicator');
        const sendBtn = document.getElementById('sendBtn');
        const endpointUrl = '${pageContext.request.contextPath}/assistant';

        function scrollToBottom() {
            if (messagesContainer) {
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            }
        }
        window.addEventListener('load', scrollToBottom);

        function askSuggestion(text) {
            sendMessage(text);
        }

        function handleFormSubmit(e) {
            e.preventDefault();
            const text = userInput.value.trim();
            if (text) {
                userInput.value = '';
                sendMessage(text);
            }
        }

        let isSending = false;

        async function sendMessage(text) {
            if (!text || !text.trim() || isSending) return;
            isSending = true;
            const cleanText = text.trim();

            const now = new Date();
            const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            
            const userRow = document.createElement('div');
            userRow.className = 'chat-msg-row user';
            userRow.innerHTML = `
                <div class="chat-bubble">
                    ` + escapeHtml(cleanText) + `
                    <div class="chat-bubble-footer">
                        <span class="mono">` + timeStr + `</span>
                    </div>
                </div>
            `;
            messagesContainer.insertBefore(userRow, typingIndicator);
            typingIndicator.style.display = 'flex';
            scrollToBottom();

            sendBtn.disabled = true;
            sendBtn.innerHTML = `<span>Thinking...</span>`;

            try {
                const formData = new URLSearchParams();
                formData.append('message', cleanText);
                formData.append('format', 'json');

                const response = await fetch(endpointUrl, {
                    method: 'POST',
                    headers: { 
                        'Content-Type': 'application/x-www-form-urlencoded', 
                        'X-Requested-With': 'XMLHttpRequest' 
                    },
                    body: formData.toString()
                });

                if (!response.ok) {
                    throw new Error('HTTP status ' + response.status);
                }

                const data = await response.json();

                const assistantRow = document.createElement('div');
                assistantRow.className = 'chat-msg-row assistant';
                const sourceTag = data.fromAI ? 'Gemini AI' : 'Concierge Guide';
                const formattedMsg = data.message ? data.message.replace(/\n/g, '<br>') : 'I could not process your request at this moment.';
                
                assistantRow.innerHTML = `
                    <div class="chat-bubble">
                        ` + formattedMsg + `
                        <div class="chat-bubble-footer">
                            <span class="mono">` + (data.timestamp || timeStr) + `</span>
                            <span>• ` + sourceTag + `</span>
                        </div>
                    </div>
                `;
                messagesContainer.insertBefore(assistantRow, typingIndicator);
            } catch (err) {
                console.error('[Velora AI] Error:', err);
                const errorRow = document.createElement('div');
                errorRow.className = 'chat-msg-row assistant';
                errorRow.innerHTML = `
                    <div class="chat-bubble" style="border-color: var(--danger); color: var(--danger-text);">
                        Connection issue encountered. Please re-ask or contact the Front Desk.
                    </div>
                `;
                messagesContainer.insertBefore(errorRow, typingIndicator);
            } finally {
                typingIndicator.style.display = 'none';
                sendBtn.disabled = false;
                sendBtn.innerHTML = `<span>Send</span> →`;
                isSending = false;
                userInput.focus();
                scrollToBottom();
            }
        }

        function escapeHtml(text) {
            const div = document.createElement('div');
            div.innerText = text;
            return div.innerHTML;
        }
    </script>

</body>
</html>
