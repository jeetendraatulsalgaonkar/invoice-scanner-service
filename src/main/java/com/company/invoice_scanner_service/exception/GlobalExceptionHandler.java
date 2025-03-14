package com.company.invoice_scanner_service.exception;

import com.company.invoice_scanner_service.dto.InvoiceScanResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletionException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUrlException(InvalidUrlException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.of("INVALID_URL", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(PdfNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePdfNotFoundException(PdfNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of("PDF_NOT_FOUND", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(NoIbanFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoIbanFoundException(NoIbanFoundException ex) {
        return ResponseEntity.ok(ErrorResponse.of("NO_IBAN_FOUND", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(InvalidIbansException.class)
    public ResponseEntity<ErrorResponse> handleInvalidIbansException(InvalidIbansException ex) {
        return ResponseEntity.ok(ErrorResponse.of("INVALID_IBANS_PRESENT", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of("ILLEGAL_ARGUMENT", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(BlacklistedIbanFoundException.class)
    public ResponseEntity<InvoiceScanResponse> handleBlacklistedIbanFoundException(BlacklistedIbanFoundException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                InvoiceScanResponse.builder()
                        .message("Blacklisted IBANs found")
                        .blackListedIbans(ex.getBlacklistedIbans())
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(PdfProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePdfProcessingException(PdfProcessingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of("PDF_PROCESSING", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        return ResponseEntity.internalServerError().body(ErrorResponse.of("INTERNAL_ERROR", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<?> handleCompletionException(CompletionException ex) {
        // Unwrap the CompletionException to get the actual cause
        Throwable cause = ex.getCause();

        // Handle the actual cause
        return switch (cause) {
            case BlacklistedIbanFoundException blacklistedIbanFoundException ->
                    handleBlacklistedIbanFoundException(blacklistedIbanFoundException);
            case InvalidUrlException invalidUrlException -> handleInvalidUrlException(invalidUrlException);
            case PdfNotFoundException pdfNotFoundException -> handlePdfNotFoundException(pdfNotFoundException);
            case NoIbanFoundException noIbanFoundException -> handleNoIbanFoundException(noIbanFoundException);
            case InvalidIbansException invalidIbansException -> handleInvalidIbansException(invalidIbansException);
            case IllegalArgumentException illegalArgumentException ->
                    handleIllegalArgumentException(illegalArgumentException);
            case PdfProcessingException pdfProcessingException -> handlePdfProcessingException(pdfProcessingException);
            case IOException ioException -> handleIOException(ioException);
            case RuntimeException runtimeException -> handleGenericException(runtimeException);
            case null, default ->
                // Fallback for unknown exceptions
                    ResponseEntity.internalServerError()
                            .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred", Map.of()));
        };
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleGenericException(RuntimeException ex) {
        log.error("Generic exception caught: {}", ex.getClass().getName());
        log.error("Exception message: {}", ex.getMessage());
        return ResponseEntity.internalServerError().body(ErrorResponse.of("INTERNAL_ERROR", ex.getMessage(), Map.of()));
    }
}