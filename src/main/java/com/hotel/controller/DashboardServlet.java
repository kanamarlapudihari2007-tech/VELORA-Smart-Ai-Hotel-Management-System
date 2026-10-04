package com.hotel.controller;

import com.hotel.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * DashboardServlet - Routes authenticated users to their specific role-based dashboard view.
 * Mapped to /dashboard
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        User user = (User) session.getAttribute("user");
        String role = user.getRole();

        switch (role) {
            case "MANAGER":
                response.sendRedirect("manager-tasks");
                break;

            case "STAFF":
                response.sendRedirect("staff-tasks");
                break;

            case "GUEST":
            default:
                request.getRequestDispatcher("guest-dashboard.jsp").forward(request, response);
                break;
        }
    }
}
