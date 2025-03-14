package com.company.invoice_scanner_service.controller;

import com.company.invoice_scanner_service.entity.BlacklistedIban;
import com.company.invoice_scanner_service.service.iban.BlacklistedIbanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(BlacklistedIbanControllerTest.MockConfig.class)  // Import Mock Configuration
class BlacklistedIbanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlacklistedIbanService blacklistedIbanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.reset(blacklistedIbanService);
    }

    @Test
    void shouldBlacklistIbanSuccessfully() throws Exception {
        // Mock service behavior (No exception means successful execution)
        when(blacklistedIbanService.blacklistIban(anyString(), anyString()))
                .thenReturn(new BlacklistedIban());

        mockMvc.perform(post("/api/blacklisted-ibans")
                        .param("iban", "DE89370400440532013000")
                        .param("reason", "Fraudulent activity")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("IBAN successfully blacklisted."));
    }

    @Test
    void shouldReturnBadRequestWhenIbanAlreadyBlacklisted() throws Exception {
        // Simulate exception for duplicate IBAN
        doThrow(new IllegalArgumentException("IBAN is already blacklisted: DE89370400440532013000"))
                .when(blacklistedIbanService).blacklistIban(anyString(), anyString());

        mockMvc.perform(post("/api/blacklisted-ibans")
                        .param("iban", "DE89370400440532013000")
                        .param("reason", "Fraudulent activity")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("IBAN is already blacklisted: DE89370400440532013000"));
    }

    @Test
    void shouldReturnInternalServerErrorOnUnexpectedException() throws Exception {
        // Simulate unexpected error
        doThrow(new RuntimeException("Unexpected database error"))
                .when(blacklistedIbanService).blacklistIban(anyString(), anyString());

        mockMvc.perform(post("/api/blacklisted-ibans")
                        .param("iban", "DE89370400440532013000")
                        .param("reason", "Fraudulent activity")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorMessage").value("Unexpected database error"));
    }


    // Custom configuration for creating mock beans
    static class MockConfig {
        @Bean
        public BlacklistedIbanService blacklistedIbanService() {
            return Mockito.mock(BlacklistedIbanService.class);
        }
    }
}