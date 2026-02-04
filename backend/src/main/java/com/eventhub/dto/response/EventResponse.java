package com.eventhub.dto.response;

import com.eventhub.entity.Event;
import com.eventhub.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String name,
        String description,
        LocalDateTime eventDate,
        String location,
        Integer capacity,
        Integer availableTickets,
        BigDecimal price,
        String imageUrl,
        EventStatus status,
        Integer ticketsSold,
        Double soldPercentage,
        Boolean isAvailable,
        Boolean isPast,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
        ) {
    public static EventResponse fromEntity(Event event) {
        int ticketsSold = event.getCapacity() - event.getAvailableCapacity();
        double soldPercentage = (ticketsSold * 100.0) / event.getCapacity();
        boolean isPast = event.isPast();
        boolean isAvailable = event.hasAvailableCapacity() && !isPast;

        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getEventDate(),
                event.getLocation(),
                event.getCapacity(),
                event.getAvailableCapacity(), // Maps to availableTickets
                event.getPrice(),
                event.getImageUrl(),
                event.getStatus(),
                ticketsSold,
                Math.round(soldPercentage * 100.0) / 100.0,
                isAvailable,
                isPast,
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
