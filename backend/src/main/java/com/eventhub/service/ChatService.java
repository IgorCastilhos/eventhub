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
        log.info("Processando mensagem do chat: {}", userMessage);
        try {
            String context = buildEventContext();
            String prompt = buildPrompt(context, userMessage);
            String response = callOllama(prompt);
            log.info("Resposta do chat gerada com sucesso");
            return response;
        } catch (Exception e) {
            log.error("Erro ao gerar resposta do chat no Ollama em {}: {}",
                ollamaBaseUrl, e.getMessage(), e);
            return "Desculpe, estou tendo problemas para me conectar ao meu cérebro de IA agora. " +
                    "Por favor, tente novamente mais tarde ou navegue pelos eventos diretamente!\n\n" +
                    "Detalhes técnicos: " + e.getMessage();
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
            return "Atualmente, não há eventos programados.";
        }
        StringBuilder context = new StringBuilder();
        context.append("Aqui estão os próximos eventos:\n\n");
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            context.append(String.format(
                    "Evento %d: %s\n" +
                            "Data: %s\n" +
                            "Local: %s\n" +
                            "Capacidade: %d/%d lugares disponíveis\n" +
                            "Descrição: %s\n\n",
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
                        Você é um assistente virtual útil do EventHub, uma plataforma de venda de ingressos para eventos.
                        Seu trabalho é ajudar os usuários a encontrar e aprender sobre os próximos eventos.
                        
                        IMPORTANTE: Responda SEMPRE em português brasileiro de forma clara, amigável e concisa.
                        
                        %s
                        
                        Pergunta do usuário: %s
                        
                        Forneça uma resposta amigável e concisa baseada nos eventos disponíveis.
                        Se o usuário perguntar sobre algo que não está na lista de eventos, informe educadamente
                        e sugira que ele verifique novamente mais tarde ou navegue pelo catálogo completo de eventos.
                        
                        Use emojis quando apropriado para tornar a conversa mais amigável. 😊
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
                "stream", false,
                "options", Map.of(
                    "temperature", 0.7,
                    "num_predict", 500
                )
        );

        log.info("Calling Ollama API: {} with model: {}", url, ollamaModel);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    url,
                    request,
                    Map.class
            );

            if (response != null && response.containsKey("response")) {
                String aiResponse = (String) response.get("response");
                log.debug("Ollama response received: {} characters", aiResponse.length());
                return aiResponse;
            }

            log.error("Invalid response from Ollama: {}", response);
            throw new RuntimeException("Invalid response from Ollama - missing 'response' field");
        } catch (Exception e) {
            log.error("Failed to call Ollama API at {}: {}", url, e.getMessage());
            throw new RuntimeException("Failed to connect to Ollama at " + url + ": " + e.getMessage(), e);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}