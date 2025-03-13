package com.company.invoice_scanner_service.service.iban;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IbanValidationServiceTest {

    @InjectMocks
    private IbanValidationService ibanValidationService;

    @Mock
    private Logger log;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(log);
    }

    @Test
    void testValidateIbans_ValidIbans() {
        // Arrange
        List<String> ibans = Arrays.asList("BE68539007547034", "DE89370400440532013000");

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("BE68539007547034"));
        assertTrue(result.contains("DE89370400440532013000"));
    }

    @Test
    void testValidateIbans_InvalidIbans() {
        // Arrange
        List<String> ibans = Arrays.asList("INVALID_IBAN", "BE6853900754703"); // Invalid IBANs

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testValidateIbans_UnsupportedCountry() {
        // Arrange
        List<String> ibans = Collections.singletonList("XX12345678901234567890"); // Unsupported country

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testValidateIbans_EmptyList() {
        // Arrange
        List<String> ibans = Collections.emptyList();

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testValidateIbans_NullList() {
        // Arrange
        List<String> ibans = null;

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testValidateIbans_ValidAndInvalidIbans() {
        // Arrange
        List<String> ibans = Arrays.asList("BE68539007547034", "INVALID_IBAN", "DE89370400440532013000");

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("BE68539007547034"));
        assertTrue(result.contains("DE89370400440532013000"));
    }

    @Test
    void testValidateIbans_InvalidFormat() {
        // Arrange
        List<String> ibans = Collections.singletonList("BE68-5390-0754-70345"); // Invalid format due to hyphens

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testValidateIbans_InvalidChecksum() {
        // Arrange
        // Use a structurally valid IBAN but with an invalid checksum
        List<String> ibans = Collections.singletonList("BE0000000000000000"); // Invalid checksum

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertTrue(result.isEmpty());
    }
}