package com.company.invoice_scanner_service.controller;

import com.company.invoice_scanner_service.dto.InvoiceScanResponse;
import com.company.invoice_scanner_service.exception.BlacklistedIbanFoundException;
import com.company.invoice_scanner_service.exception.ErrorResponse;
import com.company.invoice_scanner_service.exception.InvalidUrlException;
import com.company.invoice_scanner_service.exception.NoIbanFoundException;
import com.company.invoice_scanner_service.service.TaskOrchestratorService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
class InvoiceScannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskOrchestratorService taskOrchestratorService;

    @Test
    void testScanInvoices_ValidUrl_ReturnsValidResponse() throws Exception {
        String validUrl = "https://drive.usercontent.google.com/u/0/uc?id=18YCO-lsaZVxPyBLuJRWwvMUvFRWDSvLL&export=download";
        InvoiceScanResponse expectedResponse = InvoiceScanResponse.builder()
                .message("IBAN extraction successful. No blacklisted IBANs found")
                .blackListedIbans(List.of())
                .validIbans(List.of("DE44500105175407324931"))
                .timestamp(Instant.now())
                .build();

        when(taskOrchestratorService.processPdfForIbans(anyString())).thenReturn(List.of("DE44500105175407324931"));

        this.mockMvc.perform(get("/api/invoice-scanner/process")
                        .param("url", validUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedResponse.getMessage()))
                .andExpect(jsonPath("$.validIbans[0]").value(expectedResponse.getValidIbans().get(0)))
                .andExpect(jsonPath("$.blackListedIbans").doesNotExist());

        verify(taskOrchestratorService, times(1)).processPdfForIbans(validUrl);
    }

    @Test
    void testScanInvoices_InvalidUrl_ReturnsBadRequest() throws Exception {
        String invalidUrl = "invalid-url";

        when(taskOrchestratorService.processPdfForIbans(invalidUrl)).thenThrow(new InvalidUrlException("Invalid URL format: " + invalidUrl));

        mockMvc.perform(get("/api/invoice-scanner/process")
                        .param("url", invalidUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorKey").value("INVALID_URL"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid URL format: invalid-url"));
    }

    @Test
    void testScanInvoices_NoIbanFound_ReturnsNoContent() throws Exception {
        String validUrl = "https://example.com/invoice.pdf";
        ErrorResponse expectedResponse = ErrorResponse.of("NO_IBAN_FOUND", "No IBANs found in the provided documents.", Map.of());

        doThrow(new NoIbanFoundException("No IBANs found in the provided documents."))
                .when(taskOrchestratorService).processPdfForIbans(validUrl);

        mockMvc.perform(get("/api/invoice-scanner/process")
                        .param("url", validUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorKey").value(expectedResponse.errorKey()))
                .andExpect(jsonPath("$.errorMessage").value(expectedResponse.errorMessage()));
    }

    @Test
    void testScanInvoices_BlacklistedIbanFound_ReturnsOK() throws Exception {
        String validUrl = "https://example.com/invoice.pdf";
        InvoiceScanResponse expectedResponse = InvoiceScanResponse.builder()
                .message("Blacklisted IBANs found")
                .blackListedIbans(List.of("DE89370400440532013000"))
                .validIbans(List.of("DE44500105175407324931"))
                .timestamp(Instant.now())
                .build();

        doThrow(new BlacklistedIbanFoundException(List.of("DE89370400440532013000"), List.of("DE44500105175407324931")))
                .when(taskOrchestratorService).processPdfForIbans(validUrl);

        mockMvc.perform(get("/api/invoice-scanner/process")
                        .param("url", validUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", Matchers.equalTo(expectedResponse.getMessage())))
                .andExpect(jsonPath("$.blackListedIbans", Matchers.equalTo(expectedResponse.getBlackListedIbans())))
                .andExpect(jsonPath("$.validIbans", Matchers.equalTo(expectedResponse.getValidIbans())));
    }

    @Test
    void testScanInvoices_InternalServerError() throws Exception {
        String validUrl = "https://example.com/invoice.pdf";

        when(taskOrchestratorService.processPdfForIbans(validUrl)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/invoice-scanner/process")
                        .param("url", validUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorKey").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.errorMessage").value("Unexpected error"));

        verify(taskOrchestratorService, times(1)).processPdfForIbans(validUrl);
    }
}