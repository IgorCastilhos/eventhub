package com.eventhub.entity;

import com.eventhub.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "tickets",
        indexes = {
                @Index(name = "idx_ticket_event", columnList = "event_id"),
                @Index(name = "idx_ticket_user", columnList = "user_id"),
                @Index(name = "idx_ticket_confirmation", columnList = "confirmation_code"),
                @Index(name = "idx_ticket_status", columnList = "status"),
                @Index(name = "idx_ticket_purchase_date", columnList = "purchase_date")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_active_ticket_per_user_event",
                        columnNames = {"event_id", "user_id", "status"}
                )
        }
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"event", "user"}) // Prevent circular references in logs
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Ticket {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    @EqualsAndHashCode.Include
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ticket_event"))
    private Event event;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ticket_user"))
    private User user;


    @Embedded
    private Participant participant;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.ACTIVE;

    @Column(name = "confirmation_code", unique = true, nullable = false, length = 10)
    private String confirmationCode;

    @Column(name = "purchase_date", nullable = false)
    @Builder.Default
    private LocalDateTime purchaseDate = LocalDateTime.now();

    @Column(name = "check_in_at")
    private LocalDateTime checkInAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.purchaseDate == null) {
            this.purchaseDate = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void use() {
        if (!status.canBeUsed()) {
            throw new IllegalStateException(
                    "Cannot use ticket with status: " + status +
                            ". Only ACTIVE tickets can be used."
            );
        }

        if (event.isPast()) {
            throw new IllegalStateException(
                    "Cannot use ticket for past event: " + event.getName()
            );
        }

        this.status = TicketStatus.USED;
        this.checkInAt = LocalDateTime.now();
    }

    public void cancel() {
        if (!status.canBeCancelled()) {
            throw new IllegalStateException(
                    "Cannot cancel ticket with status: " + status +
                            ". Only ACTIVE tickets can be cancelled."
            );
        }

        if (event.isPast()) {
            throw new IllegalStateException(
                    "Cannot cancel ticket for past event: " + event.getName()
            );
        }

        this.status = TicketStatus.CANCELLED;

        event.cancelReservation();
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean isUsed() {
        return status.isUsed();
    }

    public boolean isCancelled() {
        return status.isCancelled();
    }

    public boolean belongsTo(UUID userId) {
        return this.user != null && this.user.getId().equals(userId);
    }

    public boolean isForParticipant(String email) {
        return this.participant != null &&
                this.participant.hasEmail(email);
    }

    public String getDisplayReference() {
        return String.format("%s - %s",
                confirmationCode,
                event != null ? event.getName() : "Unknown Event"
        );
    }

}