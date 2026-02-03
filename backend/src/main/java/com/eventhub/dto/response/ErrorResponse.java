package com.eventhub.dto.response;

import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> errors
) {
    public ErrorResponse(String message) {
        this(
                LocalDateTime.now(),
                400,
                "Bad Request",
                message,
                null,
                null
        );
    }

    public ErrorResponse(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> errors
    ) {
        this(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                errors
        );
    }
}

record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {
    public static <E, D> PageResponse<D> from(
            Page<E> page,
            Function<E, D> mapper
    ) {
        return new PageResponse<>(
                page.getContent().stream()
                        .map(mapper)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}
