package com.eventhub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(
                regexp = "^[a-zA-Z0-9][a-zA-Z0-9_-]*$",
                message = "Username must start with alphanumeric and contain only letters, numbers, underscore, and hyphen"
        )
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)"
        )
        String password
) {
    public String getNormalizedUsername() {
        return username != null ? username.toLowerCase() : null;
    }

    public String getNormalizedEmail() {
        return email != null ? email.toLowerCase() : null;
    }

    public List<String> validatePasswordStrength() {
        List<String> errors = new ArrayList<>();
        if (password == null) {
            return errors;
        }
        String lowerPassword = password.toLowerCase();
        List<String> commonPasswords = List.of(
                "password", "123456", "qwerty", "welcome", "admin"
        );

        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                errors.add("Password is too common or weak: " + common);
            }
        }

        if (username != null && lowerPassword.contains(username.toLowerCase())) {
            errors.add("Password should not contain the username");
        }

        if (lowerPassword.contains("abc") || lowerPassword.contains("1234")) {
            errors.add("Password should not contain sequential characters");
        }

        return errors;
    }
}
