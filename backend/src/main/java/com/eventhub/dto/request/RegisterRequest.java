package com.eventhub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public record RegisterRequest(
        @NotBlank(message = "Usuário é obrigatório")
        @Size(min = 3, max = 50, message = "Usuário deve ter entre 3 e 50 caracteres")
        @Pattern(
                regexp = "^[a-zA-Z0-9][a-zA-Z0-9_-]*$",
                message = "Usuário deve começar com letra ou número e conter apenas letras, números, underscore e hífen"
        )
        String username,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail deve ser válido")
        @Size(max = 255, message = "E-mail não pode exceder 255 caracteres")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}$",
                message = "Senha deve conter pelo menos uma letra maiúscula, uma letra minúscula, um número e um caractere especial (@$!%*?&)"
        )
        String password
) {
    public String getNormalizedUsername() {
        return username != null ? username.toLowerCase() : null;
    }

    public String getNormalizedEmail() {
        return email != null ? email.toLowerCase() : null;
    }

    public List<String> validatePasswordStrength() {
        List<String> errors = new ArrayList<>();
        if (password == null) {
            return errors;
        }
        String lowerPassword = password.toLowerCase();
        List<String> commonPasswords = List.of(
                "password", "123456", "qwerty", "welcome", "admin"
        );

        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                errors.add("Senha muito comum ou fraca: " + common);
            }
        }

        if (username != null && lowerPassword.contains(username.toLowerCase())) {
            errors.add("Senha não deve conter o nome de usuário");
        }

        if (lowerPassword.contains("abc") || lowerPassword.contains("1234")) {
            errors.add("Senha não deve conter caracteres sequenciais");
        }

        return errors;
    }
}
