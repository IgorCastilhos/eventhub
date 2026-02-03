package com.eventhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_event_date", columnList = "event_date"),

        @Index(name = "idx_location", columnList = "location"),

        @Index(name = "idx_date_capacity", columnList = "event_date, available_capacity")
})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Event {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    @EqualsAndHashCode.Include
    private UUID id;
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "name", nullable = false, length = 255)
    private String name;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @Column(name = "location", nullable = false, length = 500)
    private String location;
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "available_capacity", nullable = false)
    private Integer availableCapacity;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.availableCapacity == null) {
            this.availableCapacity = this.capacity;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public void reserveTicket() {
        if (this.availableCapacity <= 0) {
            throw new IllegalStateException(
                    "Event '%s' is sold out (capacity: %d)".formatted(this.name, this.capacity)
            );
        }

        if (this.eventDate.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "Cannot reserve ticket for past event '%s'".formatted(this.name)
            );
        }

        this.availableCapacity--;
    }

    public void cancelReservation() {
        if (this.availableCapacity >= this.capacity) {
            throw new IllegalStateException(
                    "Cannot cancel reservation: capacity already at maximum"
            );
        }

        this.availableCapacity++;
    }

    public boolean isSoldOut() {
        return this.availableCapacity <= 0;
    }

    public boolean isPast() {
        return this.eventDate.isBefore(LocalDateTime.now());
    }

    public double getOccupancyRate() {
        if (this.capacity == 0) {
            return 0.0;
        }

        int sold = this.capacity - this.availableCapacity;
        return (double) sold / this.capacity * 100.0;
    }

    public int getTicketsSold() {
        return this.capacity - this.availableCapacity;
    }
}