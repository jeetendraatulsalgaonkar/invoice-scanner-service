package com.company.invoice_scanner_service.service.iban;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IbanValidationServiceTest {
    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private IbanValidationService ibanValidationService;

    private static final String VALID_IBAN = "DE44500105175407324931";  // Valid IBAN (Germany)
    private static final String INVALID_IBAN = "INVALIDIBAN123";       // Invalid IBAN

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testValidateIbans_ValidIban() {
        List<String> result = ibanValidationService.validateIbans(List.of(VALID_IBAN));

        assertEquals(1, result.size());
        assertEquals(VALID_IBAN, result.getFirst());
    }

    @Test
    void testValidateIbans_InvalidIban() {

        List<String> result = ibanValidationService.validateIbans(List.of(INVALID_IBAN));

        assertTrue(result.isEmpty());
    }

    @Test
    void testValidateIbans_MultipleIbans() {

        List<String> result = ibanValidationService.validateIbans(List.of(VALID_IBAN, INVALID_IBAN));

        assertEquals(1, result.size());
        assertEquals(VALID_IBAN, result.getFirst());
    }

    @Test
    void testValidateIbans_HandlesIbanFormatException() {
        List<String> result = ibanValidationService.validateIbans(List.of(INVALID_IBAN));
        assertTrue(result.isEmpty());
    }
}