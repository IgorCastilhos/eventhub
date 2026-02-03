package com.eventhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Participant {
    @Column(name = "participant_name", nullable = false, length = 255)
    private String name;

    @Column(name = "participant_email", nullable = false, length = 255)
    private String email;

    public String getDisplayName() {
        return String.format("%s <%s>", name, email);
    }

    public boolean hasEmail(String email) {
        return this.email != null &&
                this.email.equalsIgnoreCase(email);
    }
}
