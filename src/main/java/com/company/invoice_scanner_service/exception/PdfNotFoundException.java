package com.company.invoice_scanner_service.exception;

public class PdfNotFoundException extends RuntimeException {
    public PdfNotFoundException(String message) {
        super(message);
    }
}
