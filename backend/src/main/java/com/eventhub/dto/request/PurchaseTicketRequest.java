package com.eventhub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PurchaseTicketRequest(
        @NotNull(message = "ID do evento é obrigatório")
        UUID eventId,
        @NotBlank(message = "Nome do participante é obrigatório")
        @Size(min = 2, max = 255, message = "Nome do participante deve ter entre 2 e 255 caracteres")
        String participantName,
        @NotBlank(message = "E-mail do participante é obrigatório")
        @Email(message = "E-mail do participante deve ser válido")
        @Size(max = 255, message = "E-mail do participante não pode exceder 255 caracteres")
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
