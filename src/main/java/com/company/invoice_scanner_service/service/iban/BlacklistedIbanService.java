package com.company.invoice_scanner_service.service.iban;

import com.company.invoice_scanner_service.entity.BlacklistedIban;
import com.company.invoice_scanner_service.exception.BlacklistedIbanFoundException;
import com.company.invoice_scanner_service.repository.BlacklistedIbanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistedIbanService {

    private final BlacklistedIbanRepository blacklistedIbanRepository;

    /**
     * Checks if any IBAN from the list is blacklisted.
     */
    public void checkForBlacklistedIbans(List<String> ibans) {
        List<String> validIbans = new ArrayList<>(ibans);
        List<BlacklistedIban> blacklisted = blacklistedIbanRepository.findByIbanIn(ibans);
        if (!blacklisted.isEmpty()) {
            List<String> blacklistedIbans = blacklisted.stream()
                    .map(BlacklistedIban::getIban)
                    .collect(Collectors.toList());
            validIbans.removeAll(blacklistedIbans);
            log.warn("Blacklisted IBANs detected: {}", blacklistedIbans);
            System.out.println("Throwing exception " + blacklistedIbans);
            throw new BlacklistedIbanFoundException(blacklistedIbans, validIbans);
        }
    }

    @Transactional
    public BlacklistedIban blacklistIban(String iban, String reason) {
        if (blacklistedIbanRepository.existsByIban(iban)) {
            throw new IllegalArgumentException("IBAN is already blacklisted: " + iban);
        }

        BlacklistedIban blacklistedIban = new BlacklistedIban();
        blacklistedIban.setIban(iban);
        blacklistedIban.setReason(reason);

        BlacklistedIban savedIban = blacklistedIbanRepository.save(blacklistedIban);
        log.info("Blacklisted IBAN: {}", iban);
        return savedIban;
    }
}
