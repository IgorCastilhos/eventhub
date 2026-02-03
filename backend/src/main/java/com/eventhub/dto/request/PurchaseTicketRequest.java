package com.eventhub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PurchaseTicketRequest(
        @NotNull(message = "Event ID is required")
        UUID eventId,
        @NotBlank(message = "Participant name is required")
        @Size(min = 2, max = 255, message = "Participant name must be between 2 and 255 characters")
        String participantName,
        @NotBlank(message = "Participant email is required")
        @Email(message = "Participant email must be a valid email address")
        @Size(max = 255, message = "Participant email must not exceed 255 characters")
        String participantEmail
) {
    public String getNormalizedParticipantEmail() {
        return participantEmail != null ?
                participantEmail.toLowerCase().trim() : null;
    }

    public String getNormalizedParticipantName() {
        return participantName != null ?
                participantName.trim() : null;
    }

    public boolean isDisposableEmail() {
        if (participantEmail == null) return false;
        String email = participantEmail.toLowerCase();
        String[] disposableDomains = {
                "tempmail.com",
                "10minutemail.com",
                "guerrillamail.com",
                "throwaway.email",
                "mailinator.com"
        };
        for (String domain : disposableDomains) {
            if (email.endsWith("@" + domain)) {
                return true;
            }
        }
        return false;
    }
}
