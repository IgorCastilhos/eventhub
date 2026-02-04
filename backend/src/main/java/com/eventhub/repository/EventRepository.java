package com.eventhub.repository;

import com.eventhub.entity.Event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    /**
     * Find event by ID with pessimistic write lock.
     * Used during ticket purchase to prevent concurrent modifications.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithLock(@Param("id") UUID id);

    Optional<Event> findByName(String name);

    List<Event> findByLocation(String location);

    List<Event> findByEventDateAfter(LocalDateTime date);

    List<Event> findByEventDateBefore(LocalDateTime date);

    List<Event> findByEventDateBetween(LocalDateTime start, LocalDateTime end);

    List<Event> findByAvailableCapacityGreaterThan(Integer minCapacity);

    List<Event> findByNameContainingIgnoreCase(String name);

    Page<Event> findByNameContainingIgnoreCaseAndLocation(
            String name,
            String location,
            Pageable pageable
    );

    @Query("""
            SELECT e FROM Event e 
            WHERE e.eventDate > CURRENT_TIMESTAMP 
            AND e.availableCapacity > 0
            ORDER BY e.eventDate ASC
            """)
    List<Event> findUpcomingEvents();

    @Query("""
            SELECT e FROM Event e
            WHERE (CAST(e.capacity - e.availableCapacity AS double) / e.capacity) > 0.8
            AND e.eventDate > CURRENT_TIMESTAMP 
            ORDER BY e.eventDate ASC 
            """)
    List<Event> findPopularEvents();

    @Query("""
            SELECT e FROM Event e
            WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(e.location) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            ORDER BY e.eventDate ASC
            """)
    List<Event> searchEvents(@Param("searchTerm") String searchTerm);

    @Query(
            value = """
                    SELECT e FROM Event e 
                    WHERE e.location = :location 
                    AND e.eventDate > CURRENT_TIMESTAMP
                    """,
            countQuery = """
                    SELECT COUNT(e) FROM Event e 
                    WHERE e.location = :location 
                    AND e.eventDate > CURRENT_TIMESTAMP
                    """
    )
    Page<Event> findUpcomingEventsByLocation(
            @Param("location") String location,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT 
                        e.id,
                        e.name,
                        (e.capacity - e.available_capacity) as tickets_sold,
                        e.capacity,
                        ROUND((e.capacity - e.available_capacity) * 100.0 / e.capacity, 2) as occupancy_rate
                    FROM events e
                    WHERE e.event_date > CURRENT_TIMESTAMP
                    ORDER BY occupancy_rate DESC
                    LIMIT 10
                    """,
            nativeQuery = true
    )
    List<Object[]> findTop10EventsByOccupancy();

    long countByEventDateAfter(LocalDateTime date);

    long countByLocation(String location);

    boolean existsByName(String name);

    boolean existsByLocation(String location);
}