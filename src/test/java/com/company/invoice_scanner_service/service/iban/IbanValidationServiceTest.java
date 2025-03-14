package com.company.invoice_scanner_service.service.iban;

import com.company.invoice_scanner_service.config.IbanValidationProperties;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IbanValidationServiceTest {

    @InjectMocks
    private IbanValidationService ibanValidationService;

    @Mock
    private IbanValidationProperties ibanValidationProperties;

    @Mock
    private Logger log; // Only mock if the service logs messages

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(log);
    }

    @Test
    void testValidateIbans_ValidIbans() {
        when(ibanValidationProperties.getIbanPattern()).thenReturn("^[A-Z]{2}\\d{2}[A-Z0-9]+$");
        when(ibanValidationProperties.getValidLengths()).thenReturn(Set.of(15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34));
        // Arrange
        List<String> ibans = Arrays.asList("BE68539007547034", "DE89370400440532013000");

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertEquals(2, result.size(), "All valid IBANs should be returned");
        assertTrue(result.contains("BE68539007547034"), "Valid IBAN should be in the result");
        assertTrue(result.contains("DE89370400440532013000"), "Valid IBAN should be in the result");
    }

    @Test
    void testValidateIbans_InvalidIbans() {
        when(ibanValidationProperties.getIbanPattern()).thenReturn("^[A-Z]{2}\\d{2}[A-Z0-9]+$");
        when(ibanValidationProperties.getValidLengths()).thenReturn(Set.of(15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34));
        // Arrange
        List<String> ibans = Arrays.asList("INVALID_IBAN", "BE6853900754703"); // Invalid IBANs

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertTrue(result.isEmpty(), "Invalid IBANs should be filtered out");
    }

    @Test
    void testValidateIbans_UnsupportedCountry() {
        when(ibanValidationProperties.getIbanPattern()).thenReturn("^[A-Z]{2}\\d{2}[A-Z0-9]+$");
        when(ibanValidationProperties.getValidLengths()).thenReturn(Set.of(15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34));
        // Arrange
        List<String> ibans = Collections.singletonList("XX12345678901234567890"); // Unsupported country

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertTrue(result.isEmpty(), "IBANs from unsupported countries should be filtered out");
    }

    @Test
    void testValidateIbans_EmptyList() {
        // Arrange
        List<String> ibans = Collections.emptyList();

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertTrue(result.isEmpty(), "Empty list should return an empty result");
    }

    @Test
    void testValidateIbans_NullList() {
        // Arrange
        List<String> ibans = null;

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertTrue(result.isEmpty(), "Null list should return an empty result");
    }

    @Test
    void testValidateIbans_ValidAndInvalidIbans() {
        when(ibanValidationProperties.getIbanPattern()).thenReturn("^[A-Z]{2}\\d{2}[A-Z0-9]+$");
        when(ibanValidationProperties.getValidLengths()).thenReturn(Set.of(15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34));
        // Arrange
        List<String> ibans = Arrays.asList("BE68539007547034", "INVALID_IBAN", "DE89370400440532013000");

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertEquals(2, result.size(), "Only valid IBANs should be returned");
        assertTrue(result.contains("BE68539007547034"), "Valid IBAN should be in the result");
        assertTrue(result.contains("DE89370400440532013000"), "Valid IBAN should be in the result");
    }

    @Test
    void testValidateIbans_InvalidFormat() {
        when(ibanValidationProperties.getIbanPattern()).thenReturn("^[A-Z]{2}\\d{2}[A-Z0-9]+$");
        // Arrange
        List<String> ibans = Collections.singletonList("BE68-5390-0754-70345"); // Invalid format due to hyphens

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertTrue(result.isEmpty(), "IBANs with invalid format should be filtered out");
    }

    @Test
    void testValidateIbans_InvalidChecksum() {
        when(ibanValidationProperties.getIbanPattern()).thenReturn("^[A-Z]{2}\\d{2}[A-Z0-9]+$");
        when(ibanValidationProperties.getValidLengths()).thenReturn(Set.of(15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34));
        // Arrange
        // Use a structurally valid IBAN but with an invalid checksum
        List<String> ibans = Collections.singletonList("BE0000000000000000"); // Invalid checksum

        // Act
        List<String> result = ibanValidationService.validateIbans(ibans);

        // Assert
        assertTrue(result.isEmpty(), "IBANs with invalid checksum should be filtered out");
    }

}