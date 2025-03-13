package com.company.invoice_scanner_service.service.iban;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iban4j.UnsupportedCountryException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.iban4j.IbanFormatException;
import org.iban4j.IbanUtil;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class IbanValidationService {

    /**
     * Validates a list of IBANs and caches results in Redis.
     */
    public List<String> validateIbans(List<String> ibans) {
        return ibans.stream()
                .filter(this::isValidIban)
                .collect(Collectors.toList());
    }

    /**
     * Checks if an IBAN is valid, using cache if available.
     */
    private boolean isValidIban(String iban) {
        log.info("Validating IBAN: {}", iban.replaceAll("\\s+", ""));
        return validateIban(iban.replaceAll("\\s+", ""));
    }

    /**
     * Uses Iban4j library for IBAN validation.
     */
    private boolean validateIban(String iban) {
        try {
            log.info("Reformatting IBAN: {}", iban.replace(" ", ""));
            IbanUtil.validate(iban.replace(" ", ""));
            return true;
        } catch (IbanFormatException ex) {
            log.warn("Invalid IBAN format: {}", iban);
            return false;
        } catch (UnsupportedCountryException ex) {
            log.error("Country code is not supported: {}", iban);
            return false;
        }
    }
}
