package com.company.invoice_scanner_service.service.iban;

import com.company.invoice_scanner_service.entity.BlacklistedIban;
import com.company.invoice_scanner_service.exception.BlacklistedIbanFoundException;
import com.company.invoice_scanner_service.repository.BlacklistedIbanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlacklistedIbanCheckServiceTest {

    @Mock
    private BlacklistedIbanRepository blacklistedIbanRepository;

    @InjectMocks
    private BlacklistedIbanCheckService blacklistedIbanCheckService;

    private static final String VALID_IBAN = "DE44500105175407324931";
    private static final String BLACKLISTED_IBAN = "FR7630006000011234567890189";

    @BeforeEach
    void setup() {
        when(blacklistedIbanRepository.findByIbanIn(anyList())).thenReturn(List.of()); // Default to no blacklisted IBANs
    }

    @Test
    void testCheckForBlacklistedIbans_NoBlacklistedIbans_NoExceptionThrown() {
        // Arrange
        List<String> ibans = List.of(VALID_IBAN);

        // Act & Assert (should not throw)
        assertDoesNotThrow(() -> blacklistedIbanCheckService.checkForBlacklistedIbans(ibans));

        // Verify repository call
        verify(blacklistedIbanRepository, times(1)).findByIbanIn(ibans);
    }

    @Test
    void testCheckForBlacklistedIbans_BlacklistedIban_ThrowsException() {
        // Arrange
        List<String> ibans = List.of(VALID_IBAN, BLACKLISTED_IBAN);
        BlacklistedIban iban = new BlacklistedIban();
        iban.setId(1L);
        iban.setIban(BLACKLISTED_IBAN);
        List<BlacklistedIban> blacklistedIbans = List.of(iban);

        when(blacklistedIbanRepository.findByIbanIn(ibans)).thenReturn(blacklistedIbans);

        // Act & Assert
        BlacklistedIbanFoundException thrown = assertThrows(
                BlacklistedIbanFoundException.class,
                () -> blacklistedIbanCheckService.checkForBlacklistedIbans(ibans)
        );

        // Assert Exception Contents
        assertTrue(thrown.getBlacklistedIbans().contains(BLACKLISTED_IBAN));
        assertTrue(thrown.getValidIbans().contains(VALID_IBAN));

        // Verify repository call
        verify(blacklistedIbanRepository, times(1)).findByIbanIn(ibans);
    }

    @Test
    void testCheckForBlacklistedIbans_AllIbansBlacklisted_ThrowsException() {
        // Arrange
        List<String> ibans = List.of(BLACKLISTED_IBAN);
        BlacklistedIban iban = new BlacklistedIban();
        iban.setId(1L);
        iban.setIban(BLACKLISTED_IBAN);
        List<BlacklistedIban> blacklistedIbans = List.of(iban);

        when(blacklistedIbanRepository.findByIbanIn(ibans)).thenReturn(blacklistedIbans);

        // Act & Assert
        BlacklistedIbanFoundException thrown = assertThrows(
                BlacklistedIbanFoundException.class,
                () -> blacklistedIbanCheckService.checkForBlacklistedIbans(ibans)
        );

        // Assert Exception Contents
        assertTrue(thrown.getBlacklistedIbans().contains(BLACKLISTED_IBAN));
        assertTrue(thrown.getValidIbans().isEmpty());

        // Verify repository call
        verify(blacklistedIbanRepository, times(1)).findByIbanIn(ibans);
    }

    @Test
    void testCheckForBlacklistedIbans_EmptyIbanList_NoExceptionThrown() {
        // Arrange
        List<String> ibans = List.of();

        // Act & Assert
        assertDoesNotThrow(() -> blacklistedIbanCheckService.checkForBlacklistedIbans(ibans));

        // Verify repository call (should be called but with an empty list)
        verify(blacklistedIbanRepository, times(1)).findByIbanIn(ibans);
    }
}
