package com.company.invoice_scanner_service.service;

import com.company.invoice_scanner_service.exception.InvalidIbansException;
import com.company.invoice_scanner_service.exception.NoIbanFoundException;
import com.company.invoice_scanner_service.exception.PdfProcessingException;
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
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskOrchestratorServiceTest {

    @Mock
    private PdfDownloadService pdfDownloadService;

    @Mock
    private IbanExtractionService ibanExtractionService;

    @Mock
    private IbanValidationService ibanValidationService;

    @Mock
    private BlacklistedIbanService blacklistedIbanService;

    @Mock
    private ExecutorService executorService;

    @InjectMocks
    private TaskOrchestratorService taskOrchestratorService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(pdfDownloadService, ibanExtractionService, ibanValidationService, blacklistedIbanService, executorService);
    }

    @Test
    void testProcessPdfsForIbans_Success() {
        // Arrange
        String url = "http://example.com/pdf1.pdf";
        List<String> urls = List.of(url);

        // Mock the behavior of furtherProcessPdfForIbans
        TaskOrchestratorService spyService = spy(taskOrchestratorService);
        doReturn(List.of("DE44500105175407324931")).when(spyService).furtherProcessPdfForIbans(url);

        // Act
        List<String> result = spyService.processPdfsForIbans(urls);

        // Assert
        assertEquals(1, result.size());
        assertEquals("DE44500105175407324931", result.getFirst());
        verify(spyService, times(1)).furtherProcessPdfForIbans(url);
    }

    @Test
    void testFurtherProcessPdfForIbans_NoIbansFound() throws IOException {
        // Arrange
        String url = "http://example.com/pdf1.pdf";
        File mockFile = mock(File.class);
        when(pdfDownloadService.downloadPdfs(url)).thenReturn(List.of(mockFile));
        when(ibanExtractionService.extractIbans(mockFile)).thenReturn(List.of());

        // Act & Assert
        assertThrows(NoIbanFoundException.class, () -> taskOrchestratorService.furtherProcessPdfForIbans(url));
    }

    @Test
    void testFurtherProcessPdfForIbans_InvalidIbans() throws IOException {
        // Arrange
        String url = "http://example.com/pdf1.pdf";
        File mockFile = mock(File.class);
        when(pdfDownloadService.downloadPdfs(url)).thenReturn(List.of(mockFile));
        when(ibanExtractionService.extractIbans(mockFile)).thenReturn(List.of("INVALID_IBAN"));
        when(ibanValidationService.validateIbans(List.of("INVALID_IBAN"))).thenReturn(List.of());

        // Act & Assert
        assertThrows(InvalidIbansException.class, () -> taskOrchestratorService.furtherProcessPdfForIbans(url));
    }

    @Test
    void testFurtherProcessPdfForIbans_PdfProcessingException() throws IOException {
        // Arrange
        String url = "http://example.com/pdf1.pdf";
        File mockFile = mock(File.class);
        when(pdfDownloadService.downloadPdfs(url)).thenReturn(List.of(mockFile));
        when(ibanExtractionService.extractIbans(mockFile)).thenThrow(new PdfProcessingException("Failed to extract IBANs"));

        // Act & Assert
        assertThrows(PdfProcessingException.class, () -> taskOrchestratorService.furtherProcessPdfForIbans(url));
    }

    @Test
    void testFurtherProcessPdfForIbans_Success() throws IOException {
        // Arrange
        String url = "http://example.com/pdf1.pdf";
        File mockFile = mock(File.class);
        when(pdfDownloadService.downloadPdfs(url)).thenReturn(List.of(mockFile));
        when(ibanExtractionService.extractIbans(mockFile)).thenReturn(List.of("DE44500105175407324931"));
        when(ibanValidationService.validateIbans(List.of("DE44500105175407324931")))
                .thenReturn(List.of("DE44500105175407324931"));
        doNothing().when(blacklistedIbanService).checkForBlacklistedIbans(anyList());

        // Act
        List<String> result = taskOrchestratorService.furtherProcessPdfForIbans(url);

        // Assert
        assertEquals(1, result.size());
        assertEquals("DE44500105175407324931", result.getFirst());
        verify(pdfDownloadService, times(1)).downloadPdfs(url);
        verify(ibanExtractionService, times(1)).extractIbans(mockFile);
        verify(ibanValidationService, times(1)).validateIbans(List.of("DE44500105175407324931"));
        verify(blacklistedIbanService, times(1)).checkForBlacklistedIbans(anyList());
    }
}