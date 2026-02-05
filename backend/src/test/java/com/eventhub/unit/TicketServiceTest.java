package com.eventhub.unit;

import com.eventhub.dto.request.PurchaseTicketRequest;
import com.eventhub.dto.response.TicketResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.Participant;
import com.eventhub.entity.Ticket;
import com.eventhub.entity.User;
import com.eventhub.enums.Role;
import com.eventhub.enums.TicketStatus;
import com.eventhub.exception.BusinessException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.TicketRepository;
import com.eventhub.service.EmailService;
import com.eventhub.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TicketService.
 *
 * These tests focus on the ticket purchase logic, specifically:
 * - Capacity validation (sold out events)
 * - Optimistic locking handling (concurrent purchases)
 * - Duplicate ticket prevention
 * - Past event validation
 *
 * Uses Mockito for mocking dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService Unit Tests")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TicketService ticketService;

    private User testUser;
    private Event testEvent;
    private PurchaseTicketRequest validRequest;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encoded_password")
                .role(Role.USER)
                .build();

        testEvent = Event.builder()
                .id(eventId)
                .name("Test Concert")
                .eventDate(LocalDateTime.now().plusDays(30))
                .location("Test Venue")
                .capacity(100)
                .availableCapacity(50)
                .version(0L)
                .build();

        validRequest = new PurchaseTicketRequest(
                eventId,
                "John Doe",
                "john@example.com"
        );
    }

    @Nested
    @DisplayName("Ticket Purchase Tests")
    class PurchaseTicketTests {

        @Test
        @DisplayName("Should successfully purchase ticket when capacity is available")
        void purchaseTicket_Success() {
            // Arrange
            when(eventRepository.findByIdWithLock(eventId)).thenReturn(Optional.of(testEvent));
            when(ticketRepository.userHasActiveTicketForEvent(testUser, testEvent)).thenReturn(false);
            when(ticketRepository.findByConfirmationCode(any())).thenReturn(Optional.empty());

            Ticket savedTicket = Ticket.builder()
                    .id(UUID.randomUUID())
                    .event(testEvent)
                    .user(testUser)
                    .participant(Participant.builder()
                            .name("John Doe")
                            .email("john@example.com")
                            .build())
                    .status(TicketStatus.ACTIVE)
                    .confirmationCode("ABC123")
                    .purchaseDate(LocalDateTime.now())
                    .build();

            when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
            when(eventRepository.saveAndFlush(any(Event.class))).thenReturn(testEvent);

            // Act
            TicketResponse response = ticketService.purchaseTicket(validRequest, testUser);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.confirmationCode()).isEqualTo("ABC123");

            // Verify event capacity was decremented
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).saveAndFlush(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getAvailableCapacity()).isEqualTo(49);

            // Verify email was sent
            verify(emailService).sendTicketConfirmation(any(Ticket.class));
        }

        @Test
        @DisplayName("Should throw exception when event is not found")
        void purchaseTicket_EventNotFound() {
            // Arrange
            when(eventRepository.findByIdWithLock(eventId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> ticketService.purchaseTicket(validRequest, testUser))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Event not found");

            verify(ticketRepository, never()).save(any());
            verify(emailService, never()).sendTicketConfirmation(any());
        }

        @Test
        @DisplayName("Should throw exception when event is sold out (capacity validation)")
        void purchaseTicket_EventSoldOut() {
            // Arrange
            testEvent.setAvailableCapacity(0);
            when(eventRepository.findByIdWithLock(eventId)).thenReturn(Optional.of(testEvent));

            // Act & Assert
            assertThatThrownBy(() -> ticketService.purchaseTicket(validRequest, testUser))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("sold out");

            verify(ticketRepository, never()).save(any());
            verify(emailService, never()).sendTicketConfirmation(any());
        }

        @Test
        @DisplayName("Should throw exception when user already has ticket for event")
        void purchaseTicket_DuplicateTicket() {
            // Arrange
            when(eventRepository.findByIdWithLock(eventId)).thenReturn(Optional.of(testEvent));
            when(ticketRepository.userHasActiveTicketForEvent(testUser, testEvent)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> ticketService.purchaseTicket(validRequest, testUser))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already have a ticket");

            verify(ticketRepository, never()).save(any());
            verify(emailService, never()).sendTicketConfirmation(any());
        }

        @Test
        @DisplayName("Should throw exception when event is in the past")
        void purchaseTicket_PastEvent() {
            // Arrange
            testEvent.setEventDate(LocalDateTime.now().minusDays(1));
            when(eventRepository.findByIdWithLock(eventId)).thenReturn(Optional.of(testEvent));

            // Act & Assert
            assertThatThrownBy(() -> ticketService.purchaseTicket(validRequest, testUser))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("past events");

            verify(ticketRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Concurrency Tests - Optimistic Locking")
    class ConcurrencyTests {

        @Test
        @DisplayName("Should handle optimistic locking failure gracefully")
        void purchaseTicket_OptimisticLockingFailure() {
            // Arrange
            when(eventRepository.findByIdWithLock(eventId)).thenReturn(Optional.of(testEvent));
            when(ticketRepository.userHasActiveTicketForEvent(testUser, testEvent)).thenReturn(false);
            when(ticketRepository.findByConfirmationCode(any())).thenReturn(Optional.empty());

            // Simulate optimistic locking failure (another transaction modified the event)
            when(eventRepository.saveAndFlush(any(Event.class)))
                    .thenThrow(new ObjectOptimisticLockingFailureException(Event.class, eventId));

            // Act & Assert
            assertThatThrownBy(() -> ticketService.purchaseTicket(validRequest, testUser))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("sold out");

            verify(emailService, never()).sendTicketConfirmation(any());
        }

        @Test
        @DisplayName("Should prevent overbooking with last available ticket")
        void purchaseTicket_LastTicket_Success() {
            // Arrange
            testEvent.setAvailableCapacity(1); // Last ticket
            when(eventRepository.findByIdWithLock(eventId)).thenReturn(Optional.of(testEvent));
            when(ticketRepository.userHasActiveTicketForEvent(testUser, testEvent)).thenReturn(false);
            when(ticketRepository.findByConfirmationCode(any())).thenReturn(Optional.empty());

            Ticket savedTicket = Ticket.builder()
                    .id(UUID.randomUUID())
                    .event(testEvent)
                    .user(testUser)
                    .participant(Participant.builder()
                            .name("John Doe")
                            .email("john@example.com")
                            .build())
                    .status(TicketStatus.ACTIVE)
                    .confirmationCode("XYZ789")
                    .purchaseDate(LocalDateTime.now())
                    .build();

            when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
            when(eventRepository.saveAndFlush(any(Event.class))).thenReturn(testEvent);

            // Act
            TicketResponse response = ticketService.purchaseTicket(validRequest, testUser);

            // Assert
            assertThat(response).isNotNull();

            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).saveAndFlush(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getAvailableCapacity()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Ticket Cancellation Tests")
    class CancellationTests {

        @Test
        @DisplayName("Should successfully cancel user's own ticket")
        void cancelTicket_Success() {
            // Arrange
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = Ticket.builder()
                    .id(ticketId)
                    .event(testEvent)
                    .user(testUser)
                    .participant(Participant.builder()
                            .name("John Doe")
                            .email("john@example.com")
                            .build())
                    .status(TicketStatus.ACTIVE)
                    .confirmationCode("ABC123")
                    .purchaseDate(LocalDateTime.now())
                    .build();

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

            // Act
            TicketResponse response = ticketService.cancelTicket(ticketId, testUser);

            // Assert
            assertThat(response).isNotNull();
            verify(emailService).sendTicketCancellation(any(Ticket.class));
        }

        @Test
        @DisplayName("Should throw exception when cancelling another user's ticket")
        void cancelTicket_NotOwner() {
            // Arrange
            UUID ticketId = UUID.randomUUID();
            User otherUser = User.builder()
                    .id(UUID.randomUUID())
                    .username("otheruser")
                    .build();

            Ticket ticket = Ticket.builder()
                    .id(ticketId)
                    .event(testEvent)
                    .user(otherUser)
                    .participant(Participant.builder()
                            .name("Other User")
                            .email("other@example.com")
                            .build())
                    .status(TicketStatus.ACTIVE)
                    .confirmationCode("ABC123")
                    .purchaseDate(LocalDateTime.now())
                    .build();

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            // Act & Assert
            assertThatThrownBy(() -> ticketService.cancelTicket(ticketId, testUser))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("only cancel your own tickets");
        }
    }
}
