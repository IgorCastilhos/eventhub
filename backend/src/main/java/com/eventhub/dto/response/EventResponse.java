package com.eventhub.dto.response;

import com.eventhub.entity.Event;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String name,
        String description,
        LocalDateTime eventDate,
        String location,
        Integer capacity,
        Integer availableCapacity,
        Integer ticketsSold,
        Double soldPercentage,
        Boolean isAvailable,
        Boolean isPast,
        LocalDateTime createdAt
        ) {
    public static EventResponse fromEntity(Event event) {
        int ticketsSold = event.getCapacity() - event.getAvailableCapacity();
        double soldPercentage = (ticketsSold * 100.0) / event.getCapacity();

        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getEventDate(),
                event.getLocation(),
                event.getCapacity(),
                event.getAvailableCapacity(),
                ticketsSold,
                Math.round(soldPercentage * 100.0) / 100.0,
                event.hasAvailableCapacity(),
                event.isPast(),
                event.getCreatedAt()
        );
    }
}
