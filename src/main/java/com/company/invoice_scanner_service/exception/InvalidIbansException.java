package com.company.invoice_scanner_service.exception;

public class InvalidIbansException extends RuntimeException {
    public InvalidIbansException(String message) {
        super(message);
    }
}
