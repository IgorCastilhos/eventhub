package com.eventhub.controller;

import com.eventhub.dto.request.CreateEventRequest;
import com.eventhub.dto.request.UpdateEventRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Events", description = "Event management endpoints")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Get all events", description = "Retrieve paginated list of all events")
    public ResponseEntity<Page<EventResponse>> getAllEvents(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number (0-indexed)")
            int page,

            @RequestParam(defaultValue = "20")
            @Parameter(description = "Page size")
            int size,

            @RequestParam(defaultValue = "eventDate")
            @Parameter(description = "Sort field")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            @Parameter(description = "Sort direction (asc/desc)")
            String direction
    ) {
        log.debug("GET /api/events - page: {}, size: {}", page, size);

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EventResponse> events = eventService.getAllEvents(pageable);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Retrieve single event details")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable
            @Parameter(description = "Event ID")
            UUID id
    ) {
        log.debug("GET /api/events/{}", id);

        EventResponse event = eventService.getEventById(id);

        return ResponseEntity.ok(event);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming events", description = "Get future events with available tickets")
    public ResponseEntity<List<EventResponse>> getUpcomingEvents() {
        log.debug("GET /api/events/upcoming");

        List<EventResponse> events = eventService.getUpcomingEvents();

        return ResponseEntity.ok(events);
    }

    @GetMapping("/search")
    @Operation(summary = "Search events", description = "Search events by name, description, or location")
    public ResponseEntity<List<EventResponse>> searchEvents(
            @RequestParam("q")
            @Parameter(description = "Search query")
            String query
    ) {
        log.debug("GET /api/events/search?q={}", query);

        List<EventResponse> events = eventService.searchEvents(query);

        return ResponseEntity.ok(events);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create event", description = "Create new event (admin only)")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request
    ) {
        log.info("POST /api/events - Creating event: {}", request.name());

        EventResponse event = eventService.createEvent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(event);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update event", description = "Update existing event (admin only)")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request
    ) {
        log.info("PATCH /api/events/{} - Updating event", id);

        EventResponse event = eventService.updateEvent(id, request);

        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete event", description = "Delete event (admin only, no sold tickets)")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable UUID id
    ) {
        log.info("DELETE /api/events/{} - Deleting event", id);

        eventService.deleteEvent(id);

        return ResponseEntity.noContent().build();
    }
}