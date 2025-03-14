package com.company.invoice_scanner_service.controller;

import com.company.invoice_scanner_service.dto.InvoiceScanResponse;
import com.company.invoice_scanner_service.exception.ErrorResponse;
import com.company.invoice_scanner_service.service.TaskOrchestratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/invoice-scanner")
@RequiredArgsConstructor
public class InvoiceScannerController {

    private final TaskOrchestratorService taskOrchestratorService;

    /**
     * Endpoint to process PDFs from a given URL and extract IBANs.
     */
    @Operation(
            summary = "Process PDFs from a given URLs and extract IBANs",
            description = "Downloads PDF files, extracts IBANs, validates them, and checks against a blacklist",
            responses = {
                    @ApiResponse(responseCode = "200", description = "IBAN extraction successful",
                            content = @Content(schema = @Schema(example = "{ \"message\": \"IBAN extraction successful.\", \"validIbans\": [\"DE44500105175407324931\"], \"timestamp\": \"2025-03-12T12:00:00Z\" }"))),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/process")
    public ResponseEntity<?> processInvoices(@RequestBody List<String> urls) {
        log.info("Received request to process PDFs from URL: {}", String.join(",", urls));
        List<String> validIbans = taskOrchestratorService.processPdfsForIbans(urls);

        return ResponseEntity.ok(
            InvoiceScanResponse.builder()
                .validIbans(validIbans)
                .message("IBAN extraction successful. No blacklisted IBANs found")
                .timestamp(Instant.now())
                .build()
        );
    }
}
