package com.company.invoice_scanner_service.service;

import com.company.invoice_scanner_service.exception.InvalidIbansException;
import com.company.invoice_scanner_service.exception.NoIbanFoundException;
import com.company.invoice_scanner_service.exception.PdfProcessingException;
import com.company.invoice_scanner_service.service.iban.BlacklistedIbanService;
import com.company.invoice_scanner_service.service.iban.IbanExtractionService;
import com.company.invoice_scanner_service.service.iban.IbanValidationService;
import com.company.invoice_scanner_service.service.pdf.PdfDownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskOrchestratorService {

    private final PdfDownloadService pdfDownloadService;
    private final IbanExtractionService ibanExtractionService;
    private final IbanValidationService ibanValidationService;
    private final BlacklistedIbanService blacklistedIbanService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Processes PDFs from a list of URLs and extracts IBANs.
     */
    public List<String> processPdfsForIbans(List<String> urls) {
        List<CompletableFuture<List<String>>> futures = urls.stream()
                .map(url -> CompletableFuture.supplyAsync(() -> furtherProcessPdfForIbans(url), executorService))
                .toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        return allFutures.thenApply(v ->
                        futures.stream()
                                .flatMap(future -> future.join().stream())
                                .collect(Collectors.toList()))
                .join();
    }

    /**
     * Orchestrates the entire process of downloading PDFs, extracting IBANs, validating them, and checking for blacklists.
     */
    List<String> furtherProcessPdfForIbans(String pdfUrl) {
        log.info("Starting processing for URL: {}", pdfUrl);

        // Step 1: Download PDF files
        List<File> pdfFiles = pdfDownloadService.downloadPdfs(pdfUrl);

        // Step 2: Extract IBANs concurrently
        List<String> extractedIbans = pdfFiles.parallelStream()
                .flatMap(file -> {
                    try {
                        return ibanExtractionService.extractIbans(file).stream();
                    } catch (Exception e) {
                        log.error("Error extracting IBANs from file: {}", file.getName(), e);
                        throw new PdfProcessingException("Failed to extract IBANs from the provided URL: " + file.getName(), e);
                    }
                })
                .distinct()
                .collect(Collectors.toList());

        if (extractedIbans.isEmpty()) {
            throw new NoIbanFoundException("No IBANs found in the provided documents.");
        }

        // Step 3: Validate IBANs using cache
        List<String> validIbans = ibanValidationService.validateIbans(extractedIbans);
        if (validIbans.isEmpty()) {
            throw new InvalidIbansException(
                    "Extracted IBANs are not valid.: " + String.join(",", extractedIbans));
        }

        // Step 4: Check for blacklisted IBANs
        blacklistedIbanService.checkForBlacklistedIbans(validIbans);

        log.info("Successfully processed IBANs: {}", validIbans);
        // Step 5: If no blacklisted IBANs are found, return the valid IBANs
        return validIbans;
    }
}
