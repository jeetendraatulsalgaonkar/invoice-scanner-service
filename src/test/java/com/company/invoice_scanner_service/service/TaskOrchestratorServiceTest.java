package com.company.invoice_scanner_service.service;

import com.company.invoice_scanner_service.exception.*;
import com.company.invoice_scanner_service.service.iban.BlacklistedIbanService;
import com.company.invoice_scanner_service.service.iban.IbanExtractionService;
import com.company.invoice_scanner_service.service.iban.IbanValidationService;
import com.company.invoice_scanner_service.service.pdf.PdfDownloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskOrchestratorServiceTest {

    @Mock
    private PdfDownloadService pdfDownloadService;

    @Mock
    private IbanExtractionService ibanExtractionService;

    @Mock
    private IbanValidationService ibanValidationService;

    @Mock
    private BlacklistedIbanService blacklistedIbanService;

    @InjectMocks
    private TaskOrchestratorService taskOrchestratorService;

    private static final String PDF_URL = "http://example.com/sample.pdf";
    private static final File MOCK_PDF = new File("mock.pdf");
    private static final String VALID_IBAN = "DE44500105175407324931";
    private static final String INVALID_IBAN = "XX001234567890";
    private static final String BLACKLISTED_IBAN = "FR7630006000011234567890189";

    @BeforeEach
    void setup() {
        when(pdfDownloadService.downloadPdfs(PDF_URL)).thenReturn(List.of(MOCK_PDF));
    }

    @Test
    void testProcessPdfForIbans_SuccessfulProcess_ReturnsValidIbans() throws IOException {
        // Arrange
        when(ibanExtractionService.extractIbans(MOCK_PDF)).thenReturn(List.of(VALID_IBAN));
        when(ibanValidationService.validateIbans(List.of(VALID_IBAN))).thenReturn(List.of(VALID_IBAN));
        doNothing().when(blacklistedIbanService).checkForBlacklistedIbans(List.of(VALID_IBAN));

        // Act
        List<String> result = taskOrchestratorService.processPdfForIbans(PDF_URL);

        // Assert
        assertEquals(1, result.size());
        assertEquals(VALID_IBAN, result.getFirst());
        verify(pdfDownloadService, times(1)).downloadPdfs(PDF_URL);
        verify(ibanExtractionService, times(1)).extractIbans(MOCK_PDF);
        verify(ibanValidationService, times(1)).validateIbans(List.of(VALID_IBAN));
        verify(blacklistedIbanService, times(1)).checkForBlacklistedIbans(List.of(VALID_IBAN));
    }

    @Test
    void testProcessPdfForIbans_PdfDownloadFails_ThrowsPdfNotFoundException() {
        // Arrange
        when(pdfDownloadService.downloadPdfs(PDF_URL)).thenThrow(new PdfNotFoundException("PDF not found"));

        // Act & Assert
        assertThrows(PdfNotFoundException.class, () -> taskOrchestratorService.processPdfForIbans(PDF_URL));

        // Verify
        verify(pdfDownloadService, times(1)).downloadPdfs(PDF_URL);
        verifyNoInteractions(ibanExtractionService, ibanValidationService, blacklistedIbanService);
    }

    @Test
    void testProcessPdfForIbans_IbanExtractionFails_ThrowsPdfProcessingException() throws IOException {
        // Arrange
        when(ibanExtractionService.extractIbans(MOCK_PDF)).thenThrow(new PdfProcessingException("Extraction failed"));

        // Act & Assert
        assertThrows(PdfProcessingException.class, () -> taskOrchestratorService.processPdfForIbans(PDF_URL));

        // Verify
        verify(pdfDownloadService, times(1)).downloadPdfs(PDF_URL);
        verify(ibanExtractionService, times(1)).extractIbans(MOCK_PDF);
        verifyNoInteractions(ibanValidationService, blacklistedIbanService);
    }

    @Test
    void testProcessPdfForIbans_NoIbansExtracted_ThrowsNoIbanFoundException() throws IOException {
        // Arrange
        when(ibanExtractionService.extractIbans(MOCK_PDF)).thenReturn(List.of());

        // Act & Assert
        assertThrows(NoIbanFoundException.class, () -> taskOrchestratorService.processPdfForIbans(PDF_URL));

        // Verify
        verify(pdfDownloadService, times(1)).downloadPdfs(PDF_URL);
        verify(ibanExtractionService, times(1)).extractIbans(MOCK_PDF);
        verifyNoInteractions(ibanValidationService, blacklistedIbanService);
    }

    @Test
    void testProcessPdfForIbans_AllIbansInvalid_ThrowsInvalidIbansException() throws IOException {
        // Arrange
        when(ibanExtractionService.extractIbans(MOCK_PDF)).thenReturn(List.of(INVALID_IBAN));
        when(ibanValidationService.validateIbans(List.of(INVALID_IBAN))).thenReturn(List.of());

        // Act & Assert
        assertThrows(InvalidIbansException.class, () -> taskOrchestratorService.processPdfForIbans(PDF_URL));

        // Verify
        verify(pdfDownloadService, times(1)).downloadPdfs(PDF_URL);
        verify(ibanExtractionService, times(1)).extractIbans(MOCK_PDF);
        verify(ibanValidationService, times(1)).validateIbans(List.of(INVALID_IBAN));
        verifyNoInteractions(blacklistedIbanService);
    }

    @Test
    void testProcessPdfForIbans_BlacklistedIbansFound_ThrowsBlacklistedIbanFoundException() throws IOException {
        // Arrange
        when(ibanExtractionService.extractIbans(MOCK_PDF)).thenReturn(List.of(BLACKLISTED_IBAN));
        when(ibanValidationService.validateIbans(List.of(BLACKLISTED_IBAN))).thenReturn(List.of(BLACKLISTED_IBAN));
        doThrow(new BlacklistedIbanFoundException(List.of(BLACKLISTED_IBAN), List.of()))
                .when(blacklistedIbanService).checkForBlacklistedIbans(List.of(BLACKLISTED_IBAN));

        // Act & Assert
        assertThrows(BlacklistedIbanFoundException.class, () -> taskOrchestratorService.processPdfForIbans(PDF_URL));

        // Verify
        verify(pdfDownloadService, times(1)).downloadPdfs(PDF_URL);
        verify(ibanExtractionService, times(1)).extractIbans(MOCK_PDF);
        verify(ibanValidationService, times(1)).validateIbans(List.of(BLACKLISTED_IBAN));
        verify(blacklistedIbanService, times(1)).checkForBlacklistedIbans(List.of(BLACKLISTED_IBAN));
    }
}
