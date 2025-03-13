package com.company.invoice_scanner_service.exception;

public class NoIbanFoundException extends RuntimeException {
    public NoIbanFoundException(String message) {
        super(message);
    }
}
