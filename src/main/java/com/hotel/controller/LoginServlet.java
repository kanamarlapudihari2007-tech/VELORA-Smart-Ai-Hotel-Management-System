package com.hotel.controller;

import com.hotel.dao.UserDAO;
import com.hotel.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * LoginServlet - Handles User Authentication and Session Management.
 * Mapped to /login
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Please provide both username and password.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        User user = userDAO.authenticate(username.trim(), password);

        if (user != null) {
            // Create session and store user details
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            session.setAttribute("userId", user.getId());

            // Redirect based on role
            switch (user.getRole()) {
                case "MANAGER":
                    response.sendRedirect("dashboard?role=manager");
                    break;
                case "STAFF":
                    response.sendRedirect("dashboard?role=staff");
                    break;
                case "GUEST":
                default:
                    response.sendRedirect("dashboard?role=guest");
                    break;
            }
        } else {
            request.setAttribute("errorMessage", "Invalid username or password. Please try again.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}
