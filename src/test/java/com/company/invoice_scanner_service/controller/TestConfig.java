package com.company.invoice_scanner_service.controller;

import com.company.invoice_scanner_service.service.TaskOrchestratorService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @Bean
    public TaskOrchestratorService taskOrchestratorService() {
        return Mockito.mock(TaskOrchestratorService.class);
    }
}
