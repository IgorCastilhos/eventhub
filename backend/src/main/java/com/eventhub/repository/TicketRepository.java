package com.eventhub.repository;

import com.eventhub.entity.Event;
import com.eventhub.entity.Ticket;
import com.eventhub.entity.User;
import com.eventhub.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findByConfirmationCode(String confirmationCode);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.event WHERE t.confirmationCode = :code")
    Optional<Ticket> findByConfirmationCodeWithEvent(@Param("code") String confirmationCode);

    List<Ticket> findByUserId(User user);

    Page<Ticket> findByUser(User user, Pageable pageable);

    List<Ticket> findByUserAndStatus(User user, TicketStatus status);

    @Query("SELECT t FROM Ticket t WHERE t.user = :user AND t.status = 'ACTIVE'")
    List<Ticket> findActiveTicketsByUser(@Param("user") User user);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.event WHERE t.user = :user")
    Page<Ticket> findByUserWithEvent(@Param("user") User user, Pageable pageable);

    List<Ticket> findByEvent(Event event);

    List<Ticket> findByEventAndStatus(Event event, TicketStatus status);

    long countByEvent(Event event);

    long countByEventAndStatus(Event event, TicketStatus status);

    @Query(
            value = """
                    SELECT 
                        COUNT(*) as total_sold,
                        SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as active,
                        SUM(CASE WHEN status = 'USED' THEN 1 ELSE 0 END) as used,
                        SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled,
                        ROUND(
                            SUM(CASE WHEN status = 'USED' THEN 1 ELSE 0 END) * 100.0 / 
                            NULLIF(COUNT(*), 0), 
                            2
                        ) as check_in_rate
                    FROM tickets
                    WHERE event_id = :eventId
                    """,
            nativeQuery = true
    )
    Object[] getEventAttendanceStatistics(@Param("eventId") UUID eventId);

    List<Ticket> findByParticipantEmail(String email);

    @Query("""
            SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END 
            FROM Ticket t 
            WHERE t.user = :user 
            AND t.event = :event 
            AND t.status = 'ACTIVE'
            """)
    boolean userHasActiveTicketForEvent(@Param("user") User user, @Param("event") Event event);

    List<Ticket> findByPurchaseDateBetween(LocalDateTime start, LocalDateTime end);

    long countByPurchaseDateBetween(LocalDateTime start, LocalDateTime end);

    @Query(
            value = """
                    SELECT 
                        DATE(purchase_date) as sale_date,
                        COUNT(*) as tickets_sold,
                        COUNT(DISTINCT user_id) as unique_buyers,
                        COUNT(DISTINCT event_id) as events_with_sales
                    FROM tickets
                    WHERE purchase_date BETWEEN :start AND :end
                    GROUP BY DATE(purchase_date)
                    ORDER BY sale_date DESC
                    """,
            nativeQuery = true
    )
    List<Object[]> getSalesStatistics(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT t FROM Ticket t 
            JOIN t.event e 
            WHERE t.status = 'ACTIVE' 
            AND e.eventDate > CURRENT_TIMESTAMP 
            AND e.eventDate <= CURRENT_TIMESTAMP + :hours HOUR
            """)
    List<Ticket> findTicketsForUpcomingEvents(@Param("hours") int hoursUntilEvent);

    @Query("""
            SELECT t FROM Ticket t 
            JOIN t.event e 
            WHERE t.status = 'ACTIVE' 
            AND e.eventDate < CURRENT_TIMESTAMP
            """)
    List<Ticket> findUnusedTicketsForPastEvents();
}
