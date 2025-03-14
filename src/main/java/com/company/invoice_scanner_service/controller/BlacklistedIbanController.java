package com.company.invoice_scanner_service.controller;

import com.company.invoice_scanner_service.exception.ErrorResponse;
import com.company.invoice_scanner_service.service.iban.BlacklistedIbanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blacklisted-ibans")
@RequiredArgsConstructor
@Slf4j
public class BlacklistedIbanController {

    private final BlacklistedIbanService blacklistedIbanService;

    @PostMapping
    @Operation(summary = "Blacklist an IBAN", description = "Adds an IBAN to the blacklist with the provided reason.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "IBAN successfully blacklisted.",
                    content = @Content(schema = @Schema(example = "IBAN successfully blacklisted."))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid IBAN or reason provided",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected error during processing",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<String> blacklistIban(
            @RequestParam @Schema(description = "IBAN to be blacklisted") String iban,
            @RequestParam @Schema(description = "Reason for blacklisting the IBAN") String reason
    ) {
        blacklistedIbanService.blacklistIban(iban, reason);
        return ResponseEntity.ok("IBAN successfully blacklisted.");
    }
}
