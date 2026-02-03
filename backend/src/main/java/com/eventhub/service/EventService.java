package com.eventhub.service;

import com.eventhub.dto.request.CreateEventRequest;
import com.eventhub.dto.request.UpdateEventRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.entity.Event;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.exception.BusinessException;
import com.eventhub.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;


    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Creating event: {}", request.name());
        if (request.eventDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    "Event date must be in the future"
            );
        }
        if (eventRepository.existsByName(request.name())) {
            log.warn("Event with similar name already exists: {}", request.name());
        }
        Event event = Event.builder()
                .name(request.name())
                .description(request.description())
                .eventDate(request.eventDate())
                .location(request.location())
                .capacity(request.capacity())
                .availableCapacity(request.capacity())
                .build();
        Event saved = eventRepository.save(event);
        log.info("Event created successfully: {} (ID: {})",
                saved.getName(), saved.getId());
        return EventResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(UUID id) {
        log.debug("Fetching event by ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found with ID: " + id
                ));

        return EventResponse.fromEntity(event);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        log.debug("Fetching all events with pagination");

        Page<Event> events = eventRepository.findAll(pageable);

        return events.map(EventResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getUpcomingEvents() {
        log.debug("Fetching upcoming events");

        List<Event> events = eventRepository.findUpcomingEvents();

        return events.stream()
                .map(EventResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponse> searchEvents(String searchTerm) {
        log.debug("Searching events with term: {}", searchTerm);

        List<Event> events = eventRepository.searchEvents(searchTerm);

        return events.stream()
                .map(EventResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getEventsByLocation(
            String location,
            Pageable pageable
    ) {
        log.debug("Fetching events by location: {}", location);

        Page<Event> events = eventRepository.findUpcomingEventsByLocation(
                location,
                pageable
        );

        return events.map(EventResponse::fromEntity);
    }

    @Transactional
    public EventResponse updateEvent(UUID id, UpdateEventRequest request) {
        log.info("Updating event: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found with ID: " + id
                ));
        if (event.isPast()) {
            throw new BusinessException(
                    "Cannot update events that have already occurred"
            );
        }
        if (!request.hasAnyUpdate()) {
            throw new BusinessException("No fields to update");
        }
        if (request.name() != null) {
            event.updateName(request.name());
        }
        if (request.description() != null) {
            event.setDescription(request.description());
        }
        if (request.eventDate() != null) {
            if (request.eventDate().isBefore(LocalDateTime.now())) {
                throw new BusinessException(
                        "Event date must be in the future"
                );
            }
            event.setEventDate(request.eventDate());
        }
        if (request.location() != null) {
            event.setLocation(request.location());
        }
        if (request.capacity() != null) {
            updateCapacity(event, request);
        }
        Event updated = eventRepository.save(event);
        log.info("Event updated successfully: {} (ID: {})",
                updated.getName(), updated.getId());
        return EventResponse.fromEntity(updated);
    }

    private void updateCapacity(Event event, UpdateEventRequest request) {
        Integer newCapacity = request.capacity();
        Integer currentCapacity = event.getCapacity();
        Integer ticketsSold = currentCapacity - event.getAvailableCapacity();
        String error = request.validateCapacityUpdate(
                currentCapacity,
                ticketsSold
        );
        if (error != null) {
            throw new BusinessException(error);
        }
        int additionalCapacity = newCapacity - currentCapacity;
        event.setCapacity(newCapacity);
        event.setAvailableCapacity(
                event.getAvailableCapacity() + additionalCapacity
        );
        log.info("Event capacity updated: {} → {} (added {} seats)",
                currentCapacity, newCapacity, additionalCapacity);
    }


    @Transactional
    public void deleteEvent(UUID id) {
        log.info("Deleting event: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found with ID: " + id
                ));
        int ticketsSold = event.getCapacity() - event.getAvailableCapacity();
        if (ticketsSold > 0) {
            throw new BusinessException(
                    String.format(
                            "Cannot delete event with sold tickets. " +
                                    "%d tickets have been sold. " +
                                    "Consider cancelling the event instead.",
                            ticketsSold
                    )
            );
        }
        eventRepository.delete(event);
        log.info("Event deleted successfully: {}", event.getName());
    }

    private Event findEventById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found with ID: " + id
                ));
    }
}