package com.hotel.controller;

import com.hotel.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * DBTestServlet - Stage 3 Verification Servlet.
 * Tests JDBC database connectivity, displays DB metadata, and counts sample records.
 */
@WebServlet("/db-test")
public class DBTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>AI Smart Hotel - JDBC Test</title>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Arial, sans-serif; background: #0f172a; color: #f8fafc; padding: 40px; display: flex; justify-content: center; align-items: center; min-height: 80vh; }");
        out.println(".card { background: #1e293b; border-radius: 16px; padding: 36px; box-shadow: 0 15px 30px rgba(0,0,0,0.5); border: 1px solid #334155; max-width: 600px; width: 100%; text-align: left; }");
        out.println(".badge-success { background: #10b981; color: #022c22; font-weight: bold; padding: 6px 16px; border-radius: 9999px; font-size: 0.85rem; display: inline-block; margin-bottom: 16px; }");
        out.println(".badge-error { background: #ef4444; color: #450a0a; font-weight: bold; padding: 6px 16px; border-radius: 9999px; font-size: 0.85rem; display: inline-block; margin-bottom: 16px; }");
        out.println("h1 { margin-top: 0; color: #38bdf8; font-size: 1.75rem; border-bottom: 1px solid #334155; padding-bottom: 12px; }");
        out.println("p { color: #94a3b8; line-height: 1.6; }");
        out.println(".info-item { background: #0f172a; padding: 12px 16px; border-radius: 8px; margin-bottom: 10px; font-family: monospace; font-size: 0.9rem; border-left: 4px solid #38bdf8; }");
        out.println(".table-grid { width: 100%; margin-top: 20px; border-collapse: collapse; }");
        out.println(".table-grid th, .table-grid td { padding: 10px; border: 1px solid #334155; text-align: left; font-size: 0.9rem; }");
        out.println(".table-grid th { background-color: #0f172a; color: #38bdf8; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='card'>");

        Connection conn = null;
        try {
            // Attempt to establish JDBC Connection
            conn = DBConnection.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();

            out.println("<span class='badge-success'>Stage 3 Passed — JDBC Connected</span>");
            out.println("<h1>MySQL Database Connection Active</h1>");

            out.println("<div class='info-item'>");
            out.println("<strong>Database Product:</strong> " + metaData.getDatabaseProductName() + "<br>");
            out.println("<strong>Product Version:</strong> " + metaData.getDatabaseProductVersion() + "<br>");
            out.println("<strong>Driver Name:</strong> " + metaData.getDriverName() + "<br>");
            out.println("<strong>URL:</strong> " + metaData.getURL());
            out.println("</div>");

            out.println("<h3>Table Record Summary:</h3>");
            out.println("<table class='table-grid'>");
            out.println("<tr><th>Table Name</th><th>Record Count</th><th>Status</th></tr>");

            String[] targetTables = {"users", "guests", "staff", "room_types", "rooms"};
            Statement stmt = conn.createStatement();

            for (String tableName : targetTables) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
                int count = 0;
                if (rs.next()) {
                    count = rs.getInt(1);
                }
                rs.close();
                out.println("<tr><td>" + tableName + "</td><td>" + count + "</td><td style='color:#10b981;'>READY</td></tr>");
            }
            stmt.close();

            out.println("</table>");

        } catch (Exception e) {
            out.println("<span class='badge-error'>Stage 3 Connection Failed</span>");
            out.println("<h1>Database Connection Error</h1>");
            out.println("<p>Failed to establish connection to MySQL server.</p>");
            out.println("<div class='info-item' style='border-left-color: #ef4444; color: #fca5a5;'>");
            out.println("<strong>Error Message:</strong> " + e.getMessage() + "<br><br>");
            out.println("<strong>Checklist:</strong><br>");
            out.println("1. Is MySQL server running on localhost:3306?<br>");
            out.println("2. Did you execute <code>schema.sql</code> to create <code>hotel_db</code>?<br>");
            out.println("3. Check username/password in <code>com.hotel.util.DBConnection</code>.");
            out.println("</div>");
        } finally {
            DBConnection.closeConnection(conn);
        }

        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}
