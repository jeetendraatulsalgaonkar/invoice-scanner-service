package com.company.invoice_scanner_service.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class BlacklistedIbanFoundException extends RuntimeException {

    private final List<String> blacklistedIbans;
    private final List<String> validIbans;

    public BlacklistedIbanFoundException(List<String> blacklistedIbans, List<String> validIbans) {
        super("Blacklisted IBANs found: " + blacklistedIbans);
        this.blacklistedIbans = blacklistedIbans;
        this.validIbans = validIbans;
    }
}
