package com.company.invoice_scanner_service.exception;

import com.company.invoice_scanner_service.dto.InvoiceScanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleInvalidUrlException_ReturnsBadRequest() {
        InvalidUrlException ex = new InvalidUrlException("Invalid URL provided");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidUrlException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("INVALID_URL", Objects.requireNonNull(response.getBody()).errorKey());
        assertEquals("Invalid URL provided", response.getBody().errorMessage());
    }

    @Test
    void testHandlePdfNotFoundException_ReturnsNotFound() {
        PdfNotFoundException ex = new PdfNotFoundException("PDF not found");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePdfNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("PDF_NOT_FOUND", Objects.requireNonNull(response.getBody()).errorKey());
        assertEquals("PDF not found", response.getBody().errorMessage());
    }

    @Test
    void testHandleNoIbanFoundException_ReturnsOk() {
        NoIbanFoundException ex = new NoIbanFoundException("No IBANs found");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNoIbanFoundException(ex);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("NO_IBAN_FOUND", Objects.requireNonNull(response.getBody()).errorKey());
        assertEquals("No IBANs found", response.getBody().errorMessage());
    }

    @Test
    void testHandleInvalidIbansException_ReturnsOk() {
        InvalidIbansException ex = new InvalidIbansException("Invalid IBANs detected");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidIbansException(ex);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("INVALID_IBANS_PRESENT", Objects.requireNonNull(response.getBody()).errorKey());
        assertEquals("Invalid IBANs detected", response.getBody().errorMessage());
    }

    @Test
    void testHandleBlacklistedIbanFoundException_ReturnsValidResponse() {
        // Arrange
        List<String> blacklistedIbans = List.of("FR7630006000011234567890189");
        List<String> validIbans = List.of("DE44500105175407324931");
        BlacklistedIbanFoundException ex = new BlacklistedIbanFoundException(blacklistedIbans, validIbans);

        // Act
        ResponseEntity<InvoiceScanResponse> response = globalExceptionHandler.handleBlacklistedIbanFoundException(ex);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        InvoiceScanResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Blacklisted IBANs found", responseBody.getMessage());
        assertEquals(blacklistedIbans, responseBody.getBlackListedIbans());
        assertNull(responseBody.getValidIbans());
        assertNotNull(responseBody.getTimestamp());
    }

    @Test
    void testHandlePdfProcessingException_ReturnsBadRequest() {
        PdfProcessingException ex = new PdfProcessingException("PDF processing failed");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePdfProcessingException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("PDF_PROCESSING", Objects.requireNonNull(response.getBody()).errorKey());
        assertEquals("PDF processing failed", response.getBody().errorMessage());
    }

    @Test
    void testHandleIOException_ReturnsInternalServerError() {
        IOException ex = new IOException("I/O error occurred");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIOException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", Objects.requireNonNull(response.getBody()).errorKey());
        assertEquals("I/O error occurred", response.getBody().errorMessage());
    }

    @Test
    void testHandleGenericException_ReturnsInternalServerError() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", Objects.requireNonNull(response.getBody()).errorKey());
        assertEquals("Unexpected error", response.getBody().errorMessage());
    }
}
