package com.hotel.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtil - Utility class providing BCrypt password hashing and verification.
 * Follows Rule #8: Never store plaintext passwords in the database.
 */
public class PasswordUtil {

    /**
     * Hashes a raw plaintext password using BCrypt with auto-generated salt.
     * @param plainPassword Raw password entered by user
     * @return Hashed password string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /**
     * Verifies a raw plaintext password against a stored hashed password.
     * @param plainPassword Raw password candidate
     * @param hashedPassword Stored BCrypt hashed password
     * @return true if password matches, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        // Support backward compatibility if password in database was inserted in plain text (e.g. initial seed)
        if (!hashedPassword.startsWith("$2a$") && !hashedPassword.startsWith("$2b$") && !hashedPassword.startsWith("$2y$")) {
            return plainPassword.equals(hashedPassword);
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
