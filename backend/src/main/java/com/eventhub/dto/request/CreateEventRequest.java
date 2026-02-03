package com.eventhub.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank(message = "Event name is required")
        @Size(min = 3, max = 200, message = "Event name must be between 3 and 200 characters")
        String name,

        @NotBlank(message = "Event description is required")
        @Size(min = 10, max = 5000, message = "Event description must be between 10 and 5000 characters")
        String description,

        @NotNull(message = "Event date is required")
        @Future(message = "Event date must be in the future")
        LocalDateTime eventDate,

        @NotBlank(message = "Event location is required")
        @Size(min = 3, max = 300, message = "Event location must be between 3 and 300 characters")
        String location,

        @NotNull(message = "Event capacity is required")
        @Min(value = 1, message = "Event capacity must be at least 1")
        @Max(value = 100000, message = "Event capacity must not exceed 100,000")
        Integer capacity
) {

    public boolean isReasonableFutureDate() {
        if (eventDate == null) return false;

        LocalDateTime twoYearsFromNow = LocalDateTime.now().plusYears(2);
        return eventDate.isBefore(twoYearsFromNow);
    }

    public boolean isHappeningSoon() {
        if (eventDate == null) return false;

        LocalDateTime sevenDaysFromNow = LocalDateTime.now().plusDays(7);
        return eventDate.isBefore(sevenDaysFromNow);
    }

    public String getSummary() {
        return String.format("%s at %s on %s (capacity: %d)",
                name,
                location,
                eventDate != null ? eventDate.toString() : "TBD",
                capacity
        );
    }

    public java.util.List<String> validateDescriptionQuality() {
        java.util.List<String> warnings = new java.util.ArrayList<>();

        if (description == null || description.isBlank()) {
            return warnings;
        }

        // Check for placeholder text
        String lower = description.toLowerCase();
        if (lower.contains("lorem ipsum") ||
                lower.contains("test event") ||
                lower.contains("placeholder")) {
            warnings.add("Description appears to contain placeholder text");
        }

        // Check for minimum word count
        String[] words = description.split("\\s+");
        if (words.length < 10) {
            warnings.add("Description is very short. Consider adding more details");
        }

        // Check for repeated characters (spam)
        if (description.matches("(.)\\1{10,}")) {
            warnings.add("Description contains suspicious repeated characters");
        }

        return warnings;
    }
}
