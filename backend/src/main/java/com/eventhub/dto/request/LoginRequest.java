package com.eventhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Usuário é obrigatório")
        @Size(min = 3, max = 255, message = "Usuário deve ter entre 3 e 255 caracteres")
        String username,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
        String password
) {
    public String getNormalizedUsername() {
        return username != null ? username.trim().toLowerCase() : null;
    }

    public boolean isEmailLogin(){
        return username != null && username.contains("@");
    }
}
