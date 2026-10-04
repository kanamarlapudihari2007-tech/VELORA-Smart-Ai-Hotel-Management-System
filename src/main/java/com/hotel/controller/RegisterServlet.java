package com.hotel.controller;

import com.hotel.dao.UserDAO;
import com.hotel.model.Guest;
import com.hotel.model.User;
import com.hotel.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * RegisterServlet - Handles Guest Registration.
 * Mapped to /register
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Forward request to register JSP view
        request.getRequestDispatcher("register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String idProof = request.getParameter("idProof");
        String address = request.getParameter("address");

        // Basic Validation
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().length() < 4 ||
            email == null || email.trim().isEmpty() ||
            fullName == null || fullName.trim().isEmpty()) {

            request.setAttribute("errorMessage", "All required fields must be filled. Password must be at least 4 characters.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        username = username.trim();
        email = email.trim();

        // Check if username or email exists
        if (userDAO.existsByUsername(username)) {
            request.setAttribute("errorMessage", "Username '" + username + "' is already taken. Please choose another.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        if (userDAO.existsByEmail(email)) {
            request.setAttribute("errorMessage", "Email '" + email + "' is already registered.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        // Secure password hashing
        String hashedPassword = PasswordUtil.hashPassword(password);

        User user = new User(username, hashedPassword, email, "GUEST");
        Guest guest = new Guest(0, fullName.trim(), phone != null ? phone.trim() : "", idProof != null ? idProof.trim() : "", address != null ? address.trim() : "");

        boolean success = userDAO.registerGuest(user, guest);

        if (success) {
            User registeredUser = userDAO.findByUsername(username);
            HttpSession session = request.getSession();
            session.setAttribute("user", registeredUser);
            session.setAttribute("successMessage", "Registration successful! Welcome to AI Smart Hotel.");
            response.sendRedirect("login.jsp?success=1");
        } else {
            request.setAttribute("errorMessage", "Registration failed due to a database error. Please try again.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
        }
    }
}
