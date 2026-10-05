package com.hotel.filter;

import com.hotel.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AuthenticationFilter - Enforces Role-Based Access Control (RBAC) and protects private endpoints.
 * Follows Rule #8: Session-based authentication & role-based authorization.
 */
@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Allow public paths without authentication
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("user") != null);

        if (!loggedIn) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?error=unauthorized");
            return;
        }

        User user = (User) session.getAttribute("user");
        String role = user.getRole();

        // Role-Based Authorization Guards
        if (path.startsWith("/manager") || path.startsWith("/analytics") || path.startsWith("/dynamic-pricing") ||
            path.startsWith("/apply-pricing") ||
            path.endsWith("manager-dashboard.jsp") || path.endsWith("analytics.jsp") ||
            path.endsWith("manager-reviews.jsp") || path.endsWith("dynamic-pricing.jsp")) {
            if (!"MANAGER".equals(role)) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/dashboard?error=forbidden");
                return;
            }
        } else if (path.startsWith("/staff") || path.endsWith("staff-dashboard.jsp")) {
            if (!"STAFF".equals(role) && !"MANAGER".equals(role)) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/dashboard?error=forbidden");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.equals("/") ||
               path.equals("/index.jsp") ||
               path.equals("/login") ||
               path.equals("/login.jsp") ||
               path.equals("/register") ||
               path.equals("/register.jsp") ||
               path.equals("/test") ||
               path.equals("/db-test") ||
               path.equals("/ai-test") ||
               path.equals("/assistant") ||
               path.equals("/chat") ||
               path.equals("/assistant.jsp") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg");
    }

    @Override
    public void destroy() {
    }
}
