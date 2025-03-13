package com.company.invoice_scanner_service.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        String errorKey,
        String errorMessage,
        LocalDateTime timestamp,
        Map<String, Object> details
) {
    public static ErrorResponse of(String key, String message, Map<String, Object> details) {
        return new ErrorResponse(key, message, LocalDateTime.now(), details);
    }
}
