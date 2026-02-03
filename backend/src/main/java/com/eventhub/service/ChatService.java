package com.eventhub.service;

import com.eventhub.entity.Event;
import com.eventhub.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final EventRepository eventRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:llama3.2:3b}")
    private String ollamaModel;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional(readOnly = true)
    public String chat(String userMessage) {
        log.info("Processing chat message: {}", userMessage);
        try {
            String context = buildEventContext();
            String prompt = buildPrompt(context, userMessage);
            String response = callOllama(prompt);
            log.info("Chat response generated successfully");
            return response;
        } catch (Exception e) {
            log.error("Error generating chat response", e);
            return "I'm sorry, I'm having trouble connecting to my AI brain right now. " +
                    "Please try again later or browse our events directly!";
        }
    }

    private String buildEventContext() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysFromNow = now.plusDays(30);
        List<Event> events = eventRepository.findByEventDateBetween(
                now,
                thirtyDaysFromNow
        );
        events = events.stream()
                .limit(10)
                .toList();
        if (events.isEmpty()) {
            return "Currently, there are no upcoming events scheduled.";
        }
        StringBuilder context = new StringBuilder();
        context.append("Here are the upcoming events:\n\n");
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            context.append(String.format(
                    "Event %d: %s\n" +
                            "Date: %s\n" +
                            "Location: %s\n" +
                            "Capacity: %d/%d seats available\n" +
                            "Description: %s\n\n",
                    i + 1,
                    event.getName(),
                    event.getEventDate().format(DATE_FORMATTER),
                    event.getLocation(),
                    event.getAvailableCapacity(),
                    event.getCapacity(),
                    truncate(event.getDescription(), 200)
            ));
        }
        return context.toString();
    }

    private String buildPrompt(String context, String userMessage) {
        return String.format(
                """
                        You are a helpful event assistant for EventHub, an event ticketing platform.
                        Your job is to help users find and learn about upcoming events.
                        
                        %s
                        
                        User question: %s
                        
                        Provide a friendly, concise response based on the available events.
                        If the user asks about something not in the event list, politely let them know
                        and suggest they check back later or browse the full event catalog.
                        """,
                context,
                userMessage
        );
    }

    private String callOllama(String prompt) {
        String url = ollamaBaseUrl + "/api/generate";
        Map<String, Object> request = Map.of(
                "model", ollamaModel,
                "prompt", prompt,
                "stream", false
        );
        log.debug("Calling Ollama API: {}", url);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
                url,
                request,
                Map.class
        );
        if (response != null && response.containsKey("response")) {
            return (String) response.get("response");
        }
        throw new RuntimeException("Invalid response from Ollama");
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}