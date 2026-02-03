package com.eventhub.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateEventRequest(
        @Size(min = 3, max = 200, message = "Event name must be between 3 and 200 characters")
        String name,
        @Size(min = 10, max = 5000, message = "Event description must be between 10 and 5000 characters")
        String description,
        @Future(message = "Event date must be in the future")
        LocalDateTime eventDate,
        @Size(min = 3, max = 300, message = "Event location must be between 3 and 300 characters")
        String location,
        @Min(value = 1, message = "Event capacity must be at least 1")
        @Max(value = 100000, message = "Event capacity must not exceed 100,000")
        Integer capacity
) {
    public boolean hasAnyUpdate() {
        return name != null ||
                description != null ||
                eventDate != null ||
                location != null ||
                capacity != null;
    }

    public boolean hasCriticalUpdates() {
        return eventDate != null || location != null;
    }

    public boolean hasMinorUpdatesOnly() {
        return (name != null || description != null) &&
                eventDate == null &&
                location == null &&
                capacity == null;
    }

    public String validateCapacityUpdate(Integer currentCapacity, Integer ticketsSold) {
        if (capacity == null) {
            return null;
        }

        if (capacity < currentCapacity) {
            return String.format(
                    "Cannot decrease capacity from %d to %d. " +
                            "Capacity can only be increased.",
                    currentCapacity, capacity
            );
        }

        if (capacity < ticketsSold) {
            return String.format(
                    "Cannot set capacity to %d. " +
                            "%d tickets already sold. " +
                            "Minimum capacity: %d",
                    capacity, ticketsSold, ticketsSold
            );
        }

        return null;
    }

    public java.util.List<String> getChangedFields() {
        java.util.List<String> changed = new java.util.ArrayList<>();
        if (name != null) changed.add("name");
        if (description != null) changed.add("description");
        if (eventDate != null) changed.add("eventDate");
        if (location != null) changed.add("location");
        if (capacity != null) changed.add("capacity");
        return changed;
    }
}
