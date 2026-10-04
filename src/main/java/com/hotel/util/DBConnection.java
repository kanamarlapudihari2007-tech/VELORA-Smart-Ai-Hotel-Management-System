package com.hotel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Production-ready utility class responsible for establishing JDBC connections to MySQL.
 * Securely resolves credentials from environment variables, system properties, or uncommitted .env files.
 * Never exposes hardcoded credentials in version control.
 */
public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/hotel_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";

    // JDBC Driver Class Name for MySQL 8+
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found in classpath!");
            e.printStackTrace();
        }
    }

    /**
     * Obtains an active connection to the MySQL hotel_db database.
     * @return java.sql.Connection object
     * @throws SQLException if database connection fails
     */
    public static Connection getConnection() throws SQLException {
        String dbUrl = resolveConfig("DB_URL", DEFAULT_URL);
        String dbUser = resolveConfig("DB_USER", DEFAULT_USER);
        String dbPassword = resolveConfig("DB_PASSWORD", "");

        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Resolves configuration parameters across environment, system properties, and local .env files.
     */
    private static String resolveConfig(String envKey, String fallback) {
        // Priority 1: Environment variable
        String val = System.getenv(envKey);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }

        // Priority 2: Java system property (e.g. -Ddb.password=...)
        val = System.getProperty(envKey.toLowerCase().replace('_', '.'));
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }

        // Priority 3: Local uncommitted .env file (excluded from git)
        val = resolveFromDotEnv(envKey);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }

        return fallback;
    }

    /**
     * Safely reads key-value pairs from an uncommitted local .env file.
     */
    private static String resolveFromDotEnv(String key) {
        String userDir = System.getProperty("user.dir");
        String[] candidatePaths = new String[] {
            ".env",
            "../.env",
            (userDir != null ? userDir + File.separator + ".env" : ".env"),
            "C:\\apache-tomcat-9.0.122\\.env",
            "C:\\apache-tomcat-9.0.122\\bin\\.env"
        };

        for (String path : candidatePaths) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                            continue;
                        }
                        int eq = line.indexOf('=');
                        String k = line.substring(0, eq).trim();
                        String v = line.substring(eq + 1).trim();
                        if (k.equals(key)) {
                            if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
                                v = v.substring(1, v.length() - 1);
                            }
                            return v;
                        }
                    }
                } catch (Exception ignored) {
                    // Fallthrough to next candidate
                }
            }
        }
        return null;
    }

    /**
     * Utility method to safely close open JDBC Connection.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Failed to close JDBC Connection: " + e.getMessage());
            }
        }
    }
}
