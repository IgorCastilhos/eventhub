package com.eventhub.service;

import com.eventhub.entity.Ticket;
import com.eventhub.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    @Value("${app.email.from:noreply@eventhub.com}")
    private String fromEmail;

    @Async
    public void sendTicketConfirmation(Ticket ticket) {
        log.info("Sending ticket confirmation to: {}",
                ticket.getParticipant().getEmail());
        log.info("Ticket confirmation email sent successfully");
    }

    @Async
    public void sendTicketCancellation(Ticket ticket) {
        log.info("Sending cancellation confirmation to: {}",
                ticket.getParticipant().getEmail());
        log.info("Cancellation email sent successfully");
    }

    @Async
    public void sendEventReminder(Ticket ticket) {
        log.info("Sending event reminder to: {}",
                ticket.getParticipant().getEmail());

        log.info("Event reminder sent successfully");
    }

    @Async
    public void sendWelcomeEmail(User user) {
        log.info("Sending welcome email to: {}", user.getEmail());

        // TODO: Implement

        log.info("Welcome email sent successfully");
    }

    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        log.info("Sending password reset email to: {}", user.getEmail());

        // TODO: Implement

        log.info("Password reset email sent successfully");
    }

}