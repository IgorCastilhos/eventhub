package com.eventhub.controller;

import com.eventhub.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "AI-powered event assistant")
@CrossOrigin(origins = "*")
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    @Operation(
            summary = "Chat with AI",
            description = "Ask AI assistant about events and get recommendations"
    )
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request
    ) {
        log.info("POST /api/chat - Message: {}", request.message());
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(new ChatResponse("Please provide a message"));
        }
        String aiResponse = chatService.chat(request.message());
        return ResponseEntity.ok(new ChatResponse(aiResponse));
    }

    public record ChatRequest(String message) {
    }

    public record ChatResponse(String response) {
    }
}