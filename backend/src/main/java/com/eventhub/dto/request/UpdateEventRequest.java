package com.eventhub.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateEventRequest(
        @Size(min = 3, max = 200, message = "Nome do evento deve ter entre 3 e 200 caracteres")
        String name,
        @Size(min = 10, max = 5000, message = "Descrição do evento deve ter entre 10 e 5000 caracteres")
        String description,
        @Future(message = "Data do evento deve estar no futuro")
        LocalDateTime eventDate,
        @Size(min = 3, max = 300, message = "Local do evento deve ter entre 3 e 300 caracteres")
        String location,
        @Min(value = 1, message = "Capacidade do evento deve ser no mínimo 1")
        @Max(value = 100000, message = "Capacidade do evento não pode exceder 100.000")
        Integer capacity,
        @DecimalMin(value = "0.00", message = "Preço do evento deve ser zero ou positivo")
        @DecimalMax(value = "1000000.00", message = "Preço do evento não pode exceder 1.000.000")
        BigDecimal price,
        @Size(max = 2048, message = "URL da imagem não pode exceder 2048 caracteres")
        String imageUrl
) {
    public boolean hasAnyUpdate() {
        return name != null ||
                description != null ||
                eventDate != null ||
                location != null ||
                capacity != null ||
                price != null ||
                imageUrl != null;
    }

    public boolean hasCriticalUpdates() {
        return eventDate != null || location != null;
    }

    public boolean hasMinorUpdatesOnly() {
        return (name != null || description != null || price != null || imageUrl != null) &&
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
        if (price != null) changed.add("price");
        if (imageUrl != null) changed.add("imageUrl");
        return changed;
    }
}
