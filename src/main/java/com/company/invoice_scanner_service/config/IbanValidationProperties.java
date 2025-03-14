package com.company.invoice_scanner_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Moved the properties here for better lookup and would keep the code better organized.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "iban.validation")
public class IbanValidationProperties {

    private Set<Integer> validLengths;

    private String ibanPattern;
}
