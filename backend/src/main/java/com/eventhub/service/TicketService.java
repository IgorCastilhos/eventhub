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
    // private final EmailService emailService; // TODO: Inject when ready

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
            Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Event not found with ID: " + request.eventId()
                ));
            validateEventAvailability(event);
            if (ticketRepository.userHasActiveTicketForEvent(user, event)) {
                throw new BusinessException(
                    "You already have a ticket for this event"
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
            event.reserveCapacity();
            eventRepository.save(event);
            Ticket saved = ticketRepository.save(ticket);
            log.info("Ticket purchased successfully: {} for event: {}",
                saved.getConfirmationCode(), event.getName());
            return TicketResponse.fromEntity(saved);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure: Event {} sold out",
                request.eventId());
            throw new BusinessException(
                "Sorry, this event just sold out. Please try another event."
            );
        }
    }
    private void validateEventAvailability(Event event) {
        if (event.isPast()) {
            throw new BusinessException(
                "Cannot purchase tickets for past events"
            );
        }
        if (!event.hasAvailableCapacity()) {
            throw new BusinessException(
                "Event is sold out"
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
        log.debug("Fetching ticket by ID: {}", id);
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Ticket not found with ID: " + id
            ));
        return TicketResponse.fromEntity(ticket);
    }
    @Transactional(readOnly = true)
    public TicketResponse getTicketByConfirmationCode(String confirmationCode) {
        log.debug("Fetching ticket by confirmation code: {}",
            confirmationCode);
        Ticket ticket = ticketRepository
            .findByConfirmationCodeWithEvent(confirmationCode)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Ticket not found with confirmation code: " + confirmationCode
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
        log.info("User {} cancelling ticket {}",
            user.getUsername(), ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Ticket not found with ID: " + ticketId
            ));
        if (!ticket.belongsTo(user.getId())) {
            throw new BusinessException(
                "You can only cancel your own tickets"
            );
        }
        ticket.cancel();
        Ticket cancelled = ticketRepository.save(ticket);
        log.info("Ticket cancelled successfully: {}",
            cancelled.getConfirmationCode());
        return TicketResponse.fromEntity(cancelled);
    }
    @Transactional
    public TicketResponse checkInTicket(String confirmationCode) {
        log.info("Checking in ticket: {}", confirmationCode);
        Ticket ticket = ticketRepository
            .findByConfirmationCodeWithEvent(confirmationCode)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Ticket not found with confirmation code: " + confirmationCode
            ));
        ticket.use();
        Ticket checkedIn = ticketRepository.save(ticket);
        log.info("Ticket checked in successfully: {} for {}",
            checkedIn.getConfirmationCode(),
            checkedIn.getParticipant().getName());
        return TicketResponse.fromEntity(checkedIn);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getEventTickets(UUID eventId) {
        log.debug("Fetching tickets for event: {}", eventId);
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Event not found with ID: " + eventId
            ));
        List<Ticket> tickets = ticketRepository.findByEvent(event);
        return tickets.stream()
            .map(TicketResponse::fromEntity)
            .toList();
    }
}