package com.hotel.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * TestServlet - Stage 1 Verification Servlet.
 * This Servlet verifies that Java Servlets are properly configured and working on Tomcat.
 */
@WebServlet("/test")
public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>AI Smart Hotel - Test Servlet</title>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Arial, sans-serif; background: #0f172a; color: #f8fafc; padding: 40px; display: flex; justify-content: center; align-items: center; min-height: 80vh; }");
        out.println(".card { background: #1e293b; border-radius: 12px; padding: 32px; box-shadow: 0 10px 25px rgba(0,0,0,0.5); border: 1px solid #334155; max-width: 500px; text-align: center; }");
        out.println(".badge { background: #10b981; color: #022c22; font-weight: bold; padding: 6px 16px; border-radius: 9999px; font-size: 0.875rem; display: inline-block; margin-bottom: 16px; }");
        out.println("h1 { margin-top: 0; color: #38bdf8; font-size: 1.75rem; }");
        out.println("p { color: #94a3b8; line-height: 1.6; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='card'>");
        out.println("<span class='badge'>Stage 1 Passed</span>");
        out.println("<h1>AI Smart Hotel Backend Active</h1>");
        out.println("<p>Java Servlet standard deployment succeeded! Tomcat 9 is processing requests correctly.</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}
