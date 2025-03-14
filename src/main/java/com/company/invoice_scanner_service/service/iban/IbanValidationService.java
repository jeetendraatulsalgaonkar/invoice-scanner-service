package com.company.invoice_scanner_service.service.iban;

import com.company.invoice_scanner_service.config.IbanValidationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iban4j.IbanFormatException;
import org.iban4j.IbanUtil;
import org.iban4j.UnsupportedCountryException;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class IbanValidationService {

    private final IbanValidationProperties ibanValidationProperties;

    /**
     *  **Validates a list of IBANs**
     */
    public List<String> validateIbans(List<String> ibans) {
        // Handle null input
        if (ibans == null) {
            return Collections.emptyList();
        }

        // Filter and collect valid IBANs
        return ibans.stream()
                .filter(this::isValidIban)
                .collect(Collectors.toList());
    }

    /**
     * **Checks if an IBAN is valid**
     * - Uses `Iban4j` where possible.
     * - Falls back to manual validation if the country is unsupported.
     */
    private boolean isValidIban(String iban) {
        String sanitizedIban = iban.replaceAll("\\s+", "").toUpperCase();
        log.info("Validating IBAN: {}", sanitizedIban);

        if (!isValidBasicFormat(sanitizedIban)) {
            log.warn("Invalid IBAN format: {}", sanitizedIban);
            return false;
        }

        try {
            // Try validating with Iban4j first
            IbanUtil.validate(sanitizedIban);
            return true;
        } catch (UnsupportedCountryException e) {
            /*
                Several countries are not supported by Iban4j,
                In those cases, UnsupportedCountryException is thrown.
                Hence, we have to manually validate such IBANs.
             */
            log.warn("Country not supported by Iban4j, using manual validation: {}", sanitizedIban);
            return isValidIbanChecksum(sanitizedIban);
        } catch (IbanFormatException e) {
            log.warn("Invalid IBAN format: {}", sanitizedIban);
            return false;
        }
    }

    /**
     *  **Basic format check for IBAN**
     */
    private boolean isValidBasicFormat(String iban) {
        return Pattern.compile(ibanValidationProperties.getIbanPattern()).matcher(iban).matches() &&
                ibanValidationProperties.getValidLengths().contains(iban.length());
    }

    /**
     *  **Manual IBAN Checksum Validation**
     * - Converts IBAN to numeric representation and checks modulo 97.
     */
    private boolean isValidIbanChecksum(String iban) {
        String rearrangedIban = iban.substring(4) + iban.substring(0, 4);
        String numericIban = convertIbanToDigits(rearrangedIban);
        return mod97Check(numericIban);
    }

    /**
     *  **Converts IBAN letters to digits**
     * - 'A' → 10, 'B' → 11, ..., 'Z' → 35.
     *  Prepares the iban string for modulo 97 validation.
     */
    private String convertIbanToDigits(String iban) {
        StringBuilder digits = new StringBuilder();
        for (char c : iban.toCharArray()) {
            if (Character.isDigit(c)) {
                digits.append(c);
            } else {
                digits.append(c - 'A' + 10);
            }
        }
        return digits.toString();
    }

    /**
     *  **Performs modulo 97 check**
     */
    private boolean mod97Check(String numericIban) {
        try {
            return new BigInteger(numericIban).mod(BigInteger.valueOf(97)).intValue() == 1;
        } catch (NumberFormatException e) {
            log.error("Error in checksum calculation: {}", e.getMessage());
            return false;
        }
    }
}
