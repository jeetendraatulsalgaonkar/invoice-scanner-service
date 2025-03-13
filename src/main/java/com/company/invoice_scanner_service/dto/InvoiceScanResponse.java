package com.company.invoice_scanner_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class InvoiceScanResponse {
    String message;
    List<String> validIbans;
    List<String> blackListedIbans;
    Instant timestamp;
}
