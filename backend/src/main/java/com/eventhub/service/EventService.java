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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    @CacheEvict(value = "events", allEntries = true)
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Criando evento: {}", request.name());
        if (request.eventDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    "Data do evento deve estar no futuro"
            );
        }
        if (eventRepository.existsByName(request.name())) {
            log.warn("Evento com nome similar já existe: {}", request.name());
        }
        Event event = Event.builder()
                .name(request.name())
                .description(request.description())
                .eventDate(request.eventDate())
                .location(request.location())
                .capacity(request.capacity())
                .availableCapacity(request.capacity())
                .price(request.price())
                .imageUrl(request.imageUrl())
                .build();
        Event saved = eventRepository.save(event);
        log.info("Evento criado com sucesso: {} (ID: {})",
                saved.getName(), saved.getId());
        return EventResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "events", key = "#id")
    public EventResponse getEventById(UUID id) {
        log.debug("Buscando evento por ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evento não encontrado com ID: " + id
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
    @Caching(evict = {
        @CacheEvict(value = "events", key = "#id"),
        @CacheEvict(value = "events", allEntries = true)
    })
    public EventResponse updateEvent(UUID id, UpdateEventRequest request) {
        log.info("Atualizando evento: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evento não encontrado com ID: " + id
                ));
        if (event.isPast()) {
            throw new BusinessException(
                    "Não é possível atualizar eventos que já ocorreram"
            );
        }
        if (!request.hasAnyUpdate()) {
            throw new BusinessException("Nenhum campo para atualizar");
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
        if (request.price() != null) {
            event.setPrice(request.price());
        }
        if (request.imageUrl() != null) {
            event.setImageUrl(request.imageUrl());
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
    @Caching(evict = {
        @CacheEvict(value = "events", key = "#id"),
        @CacheEvict(value = "events", allEntries = true)
    })
    public void deleteEvent(UUID id) {
        log.info("Excluindo evento: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evento não encontrado com ID: " + id
                ));
        int ticketsSold = event.getCapacity() - event.getAvailableCapacity();
        if (ticketsSold > 0) {
            throw new BusinessException(
                    String.format(
                            "Não é possível excluir evento com ingressos vendidos. " +
                                    "%d ingressos foram vendidos. " +
                                    "Considere cancelar o evento ao invés de excluí-lo.",
                            ticketsSold
                    )
            );
        }
        eventRepository.delete(event);
        log.info("Evento excluído com sucesso: {}", event.getName());
    }

    private Event findEventById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evento não encontrado com ID: " + id
                ));
    }
}