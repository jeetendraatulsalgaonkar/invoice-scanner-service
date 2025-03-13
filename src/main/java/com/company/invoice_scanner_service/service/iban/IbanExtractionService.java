package com.company.invoice_scanner_service.service.iban;

import com.company.invoice_scanner_service.exception.PdfProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class IbanExtractionService {

    // Improved regex to match IBANs with optional spaces
    private static final String IBAN_REGEX =
            "\\b([A-Z]{2}\\d{2}(?:[ \\t-]?\\d|[A-Z0-9]){13,30})\\b";

    /**
     * Extracts IBANs from a given PDF file.
     */
    public List<String> extractIbans(File pdfFile) throws IOException {
        List<String> ibans = new ArrayList<>();
        log.info("Extracting IBANs from file: {}", pdfFile.getName());
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            if (document.isEncrypted()) {
                throw new PdfProcessingException("The provided PDF file is encrypted and cannot be processed.");
            }
            // Extract text from all pages
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            String text = pdfTextStripper.getText(document);

            // Normalize and clean extracted text
            String cleanedText = normalizeText(text);

            // Find and collect all IBANs from the text
            ibans = extractIbansFromText(cleanedText);
        } catch(IOException e) {
            // Handle specific PDF errors
            if (e.getMessage().contains("Missing root object specification in trailer")) {
                throw new PdfProcessingException("The provided PDF file is corrupted or has an invalid format.", e);
            }
            throw new PdfProcessingException("Error while reading PDF file.", e);
        }
        return ibans;
    }

    /**
     * Finds IBANs using regex pattern.
     */
    private List<String> extractIbansFromText(String text) {
        List<String> ibans = new ArrayList<>();
        Pattern pattern = Pattern.compile(IBAN_REGEX);
        Matcher matcher = pattern.matcher(text);

        // Find all matches and add them to the list
        while (matcher.find()) {
            String iban = matcher.group(1).trim().replaceAll("\\s+", "");
            log.info("IBAN found: " + iban);
            ibans.add(iban);
        }
        return ibans;
    }

    /**
     * Normalizes extracted text by removing excessive whitespace and newlines.
     *
     * @param text Extracted PDF text.
     * @return Cleaned text.
     */
    private String normalizeText(String text) {
        return text.replaceAll("\\s+", " ") // Replace multiple spaces/newlines with a single space
                .replaceAll("(?<=\\b[A-Z]{2}\\d{2})[ \\t-]+", ""); // Remove spaces in IBAN
    }
}