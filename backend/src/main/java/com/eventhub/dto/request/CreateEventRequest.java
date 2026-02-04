package com.eventhub.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank(message = "Nome do evento é obrigatório")
        @Size(min = 3, max = 200, message = "Nome do evento deve ter entre 3 e 200 caracteres")
        String name,

        @NotBlank(message = "Descrição do evento é obrigatória")
        @Size(min = 10, max = 5000, message = "Descrição do evento deve ter entre 10 e 5000 caracteres")
        String description,

        @NotNull(message = "Data do evento é obrigatória")
        @Future(message = "Data do evento deve estar no futuro")
        LocalDateTime eventDate,

        @NotBlank(message = "Local do evento é obrigatório")
        @Size(min = 3, max = 300, message = "Local do evento deve ter entre 3 e 300 caracteres")
        String location,

        @NotNull(message = "Capacidade do evento é obrigatória")
        @Min(value = 1, message = "Capacidade do evento deve ser no mínimo 1")
        @Max(value = 100000, message = "Capacidade do evento não pode exceder 100.000")
        Integer capacity,

        @NotNull(message = "Preço do evento é obrigatório")
        @DecimalMin(value = "0.00", message = "Preço do evento deve ser zero ou positivo")
        @DecimalMax(value = "1000000.00", message = "Preço do evento não pode exceder 1.000.000")
        BigDecimal price,

        @Size(max = 2048, message = "URL da imagem não pode exceder 2048 caracteres")
        String imageUrl
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
