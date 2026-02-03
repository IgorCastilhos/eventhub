package com.eventhub.controller;

import com.eventhub.dto.request.PurchaseTicketRequest;
import com.eventhub.dto.response.TicketResponse;
import com.eventhub.entity.User;
import com.eventhub.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tickets", description = "Ticket purchase and management")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/purchase")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Purchase ticket", description = "Purchase ticket for event")
    public ResponseEntity<TicketResponse> purchaseTicket(
            @Valid @RequestBody PurchaseTicketRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.info("POST /api/tickets/purchase - User: {}, Event: {}",
                user.getUsername(), request.eventId());

        TicketResponse ticket = ticketService.purchaseTicket(request, user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticket);
    }

    @GetMapping("/my-tickets")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get my tickets", description = "Get current user's tickets")
    public ResponseEntity<Page<TicketResponse>> getMyTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user
    ) {
        log.debug("GET /api/tickets/my-tickets - User: {}", user.getUsername());
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("purchaseDate").descending()
        );
        Page<TicketResponse> tickets = ticketService.getUserTickets(user, pageable);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/my-tickets/active")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get active tickets", description = "Get current user's active tickets")
    public ResponseEntity<List<TicketResponse>> getMyActiveTickets(
            @AuthenticationPrincipal User user
    ) {
        log.debug("GET /api/tickets/my-tickets/active - User: {}",
                user.getUsername());

        List<TicketResponse> tickets = ticketService.getUserActiveTickets(user);

        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get ticket", description = "Get ticket details")
    public ResponseEntity<TicketResponse> getTicket(
            @PathVariable UUID id
    ) {
        log.debug("GET /api/tickets/{}", id);
        TicketResponse ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Cancel ticket", description = "Cancel/refund ticket")
    public ResponseEntity<TicketResponse> cancelTicket(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        log.info("DELETE /api/tickets/{} - User: {}", id, user.getUsername());

        TicketResponse ticket = ticketService.cancelTicket(id, user);

        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get event tickets", description = "Get all tickets for event (admin)")
    public ResponseEntity<List<TicketResponse>> getEventTickets(
            @PathVariable UUID eventId
    ) {
        log.debug("GET /api/tickets/event/{}", eventId);

        List<TicketResponse> tickets = ticketService.getEventTickets(eventId);

        return ResponseEntity.ok(tickets);
    }

    @PostMapping("/{confirmationCode}/checkin")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Check-in ticket", description = "Mark ticket as used (admin)")
    public ResponseEntity<TicketResponse> checkInTicket(
            @PathVariable String confirmationCode
    ) {
        log.info("POST /api/tickets/{}/checkin", confirmationCode);
        TicketResponse ticket = ticketService.checkInTicket(confirmationCode);
        return ResponseEntity.ok(ticket);
    }
}