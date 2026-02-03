package com.eventhub.dto.response;

import com.eventhub.entity.Ticket;
import com.eventhub.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(UUID id, String confirmationCode, TicketStatus status, LocalDateTime purchaseDate,
                             LocalDateTime checkInAt, String participantName, String participantEmail,
                             EventResponse event) {
    public static TicketResponse fromEntity(Ticket ticket) {
        return new TicketResponse(ticket.getId(), ticket.getConfirmationCode(), ticket.getStatus(), ticket.getPurchaseDate(), ticket.getCheckInAt(), ticket.getParticipant().getName(), ticket.getParticipant().getEmail(), EventResponse.fromEntity(ticket.getEvent()));
    }

    public static TicketResponse fromEntityWithoutEvent(Ticket ticket) {
        return new TicketResponse(ticket.getId(), ticket.getConfirmationCode(), ticket.getStatus(), ticket.getPurchaseDate(), ticket.getCheckInAt(), ticket.getParticipant().getName(), ticket.getParticipant().getEmail(), null);
    }
}
