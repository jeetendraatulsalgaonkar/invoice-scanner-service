package com.company.invoice_scanner_service.controller;

import com.company.invoice_scanner_service.service.iban.BlacklistedIbanService;
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
    public ResponseEntity<String> blacklistIban(@RequestParam String iban, @RequestParam String reason) {
        blacklistedIbanService.blacklistIban(iban, reason);
        return ResponseEntity.ok("IBAN successfully blacklisted.");
    }
}
