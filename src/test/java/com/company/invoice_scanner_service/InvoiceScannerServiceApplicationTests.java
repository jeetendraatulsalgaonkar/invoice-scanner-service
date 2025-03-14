package com.company.invoice_scanner_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class InvoiceScannerServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
