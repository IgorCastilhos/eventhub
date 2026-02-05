package com.eventhub.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes for demo users
 * Run this class to generate password hashes
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        System.out.println("=== Password Hash Generator ===\n");

        // Generate hash for admin password
        String adminPassword = "admin123";
        String adminHash = encoder.encode(adminPassword);
        System.out.println("Admin Password: " + adminPassword);
        System.out.println("Admin Hash: " + adminHash);
        System.out.println("Verify Admin: " + encoder.matches(adminPassword, adminHash));
        System.out.println();

        // Generate hash for user password
        String userPassword = "user123";
        String userHash = encoder.encode(userPassword);
        System.out.println("User Password: " + userPassword);
        System.out.println("User Hash: " + userHash);
        System.out.println("Verify User: " + encoder.matches(userPassword, userHash));
        System.out.println();

        // Verify existing hashes from migration
        System.out.println("=== Verifying Existing Hashes ===\n");

        String existingAdminHash = "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Ak5gO6QWRE8i";
        System.out.println("Existing Admin Hash matches 'admin123': " +
                          encoder.matches("admin123", existingAdminHash));

        String existingUserHash = "$2a$12$GZd/Ya2WTgfQcgkrPi/Fxu3q3qY3QX0HrN3GmKrXpPqOmMKVz8e5.";
        System.out.println("Existing User Hash matches 'user123': " +
                          encoder.matches("user123", existingUserHash));
    }
}
