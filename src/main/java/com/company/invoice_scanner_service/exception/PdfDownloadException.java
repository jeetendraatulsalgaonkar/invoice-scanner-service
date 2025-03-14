package com.company.invoice_scanner_service.exception;

public class PdfDownloadException extends RuntimeException {
    public PdfDownloadException(String message) {
        super(message);
    }
}
