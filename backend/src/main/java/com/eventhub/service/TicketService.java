package com.eventhub.service;

import com.eventhub.dto.request.PurchaseTicketRequest;
import com.eventhub.dto.response.TicketResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.Participant;
import com.eventhub.entity.Ticket;
import com.eventhub.entity.User;
import com.eventhub.enums.TicketStatus;
import com.eventhub.exception.BusinessException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CONFIRMATION_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

    @Transactional
    public TicketResponse purchaseTicket(
            PurchaseTicketRequest request,
            User user
    ) {
        log.info("User {} purchasing ticket for event {}",
                user.getUsername(), request.eventId());
        try {
            // Use pessimistic locking to prevent race conditions
            Event event = eventRepository.findByIdWithLock(request.eventId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Evento não encontrado com ID: " + request.eventId()
                    ));
            validateEventAvailability(event);
            if (ticketRepository.userHasActiveTicketForEvent(user, event)) {
                throw new BusinessException(
                        "Você já possui um ingresso para este evento"
                );
            }
            Participant participant = Participant.builder()
                    .name(request.getNormalizedParticipantName())
                    .email(request.getNormalizedParticipantEmail())
                    .build();
            String confirmationCode = generateConfirmationCode();
            Ticket ticket = Ticket.builder()
                    .event(event)
                    .user(user)
                    .participant(participant)
                    .status(TicketStatus.ACTIVE)
                    .confirmationCode(confirmationCode)
                    .purchaseDate(LocalDateTime.now())
                    .build();

            // Save the ticket (trigger no banco irá decrementar automaticamente)
            Ticket saved = ticketRepository.save(ticket);

            // Send confirmation email (async - won't block the response)
            emailService.sendTicketConfirmation(saved);

            log.info("Ticket purchased successfully: {} for event: {}",
                    saved.getConfirmationCode(), event.getName());
            return TicketResponse.fromEntity(saved);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Falha de bloqueio otimista: Evento {} esgotado",
                    request.eventId());
            throw new BusinessException(
                    "Desculpe, este evento acabou de esgotar. Por favor, tente outro evento."
            );
        }
    }

    private void validateEventAvailability(Event event) {
        if (event.isPast()) {
            throw new BusinessException(
                    "Não é possível comprar ingressos para eventos passados"
            );
        }
        if (!event.hasAvailableCapacity()) {
            throw new BusinessException(
                    "Evento esgotado"
            );
        }
    }

    private String generateConfirmationCode() {
        int maxAttempts = 10;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            StringBuilder code = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                int index = RANDOM.nextInt(CONFIRMATION_CHARS.length());
                code.append(CONFIRMATION_CHARS.charAt(index));
            }
            String confirmationCode = code.toString();
            if (!ticketRepository.findByConfirmationCode(confirmationCode)
                    .isPresent()) {
                return confirmationCode;
            }
            log.warn("Confirmation code collision, retrying: {}",
                    confirmationCode);
        }
        throw new BusinessException(
                "Unable to generate unique confirmation code. Please try again."
        );
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(UUID id) {
        log.debug("Buscando ingresso por ID: {}", id);
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingresso não encontrado com ID: " + id
                ));
        return TicketResponse.fromEntity(ticket);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketByConfirmationCode(String confirmationCode) {
        log.debug("Buscando ingresso por código de confirmação: {}",
                confirmationCode);
        Ticket ticket = ticketRepository
                .findByConfirmationCodeWithEvent(confirmationCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingresso não encontrado com código de confirmação: " + confirmationCode
                ));
        return TicketResponse.fromEntity(ticket);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> getUserTickets(User user, Pageable pageable) {
        log.debug("Fetching tickets for user: {}", user.getUsername());
        Page<Ticket> tickets = ticketRepository.findByUserWithEvent(
                user,
                pageable
        );
        return tickets.map(TicketResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getUserActiveTickets(User user) {
        log.debug("Fetching active tickets for user: {}", user.getUsername());

        List<Ticket> tickets = ticketRepository.findActiveTicketsByUser(user);

        return tickets.stream()
                .map(TicketResponse::fromEntity)
                .toList();
    }

    @Transactional
    public TicketResponse cancelTicket(UUID ticketId, User user) {
        log.info("Usuário {} cancelando ingresso {}",
                user.getUsername(), ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingresso não encontrado com ID: " + ticketId
                ));
        if (!ticket.belongsTo(user.getId())) {
            throw new BusinessException(
                    "Você só pode cancelar seus próprios ingressos"
            );
        }

        // Validate if ticket can be cancelled (throws exception if not)
        ticket.validateCanBeCancelled();

        // Cancel the ticket (trigger no banco irá restaurar capacidade automaticamente)
        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket cancelled = ticketRepository.save(ticket);

        // Send cancellation confirmation email (async)
        emailService.sendTicketCancellation(cancelled);

        log.info("Ingresso cancelado com sucesso: {}",
                cancelled.getConfirmationCode());
        return TicketResponse.fromEntity(cancelled);
    }

    @Transactional
    public TicketResponse checkInTicket(String confirmationCode) {
        log.info("Fazendo check-in do ingresso: {}", confirmationCode);
        Ticket ticket = ticketRepository
                .findByConfirmationCodeWithEvent(confirmationCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingresso não encontrado com código de confirmação: " + confirmationCode
                ));
        ticket.use();
        Ticket checkedIn = ticketRepository.save(ticket);
        log.info("Check-in do ingresso realizado com sucesso: {} para {}",
                checkedIn.getConfirmationCode(),
                checkedIn.getParticipant().getName());
        return TicketResponse.fromEntity(checkedIn);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getEventTickets(UUID eventId) {
        log.debug("Buscando ingressos para o evento: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evento não encontrado com ID: " + eventId
                ));
        List<Ticket> tickets = ticketRepository.findByEvent(event);
        return tickets.stream()
                .map(TicketResponse::fromEntity)
                .toList();
    }
}