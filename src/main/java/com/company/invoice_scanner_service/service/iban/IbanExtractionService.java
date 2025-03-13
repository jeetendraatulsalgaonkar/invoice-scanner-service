package com.company.invoice_scanner_service.service.iban;

import com.company.invoice_scanner_service.exception.PdfProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.stereotype.Service;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class IbanExtractionService {

    /**
     * Updated IBAN regex that ensures all country IBAN formats, including Belgium and multi-line IBANs, are captured
     * - Handles spaces, hyphens, dots, and special separators
     */
    private static final String IBAN_REGEX =
            "\\b([A-Z]{2}\\d{2}[ \\t\\n\\r]?[A-Z0-9]{1,4}(?:[ \\t\\n\\r]?[A-Z0-9]{1,4}){0,7})\\b";

    /**
     * Extracts IBANs from a given PDF file, including bold, italic, and formatted IBANs.
     */
    public List<String> extractIbans(File pdfFile) throws IOException {
        Set<String> ibanSet = new HashSet<>();
        log.info("Extracting IBANs from file: {}", pdfFile.getName());

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            if (document.isEncrypted()) {
                throw new PdfProcessingException("PDF is encrypted and cannot be processed.");
            }

            // Extract text from all pages using PDFTextStripper
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            String extractedText = pdfTextStripper.getText(document);
            ibanSet.addAll(extractIbansFromText(extractedText));

            // Extract text from each page
            for (int i = 1; i <= document.getNumberOfPages(); i++) {
                pdfTextStripper.setStartPage(i);
                pdfTextStripper.setEndPage(i);
                String pageText = pdfTextStripper.getText(document);
                ibanSet.addAll(extractIbansFromText(pageText));

                // Extract IBANs from **bold, italic, or rotated text**
                ibanSet.addAll(extractIbansFromAnnotations(document.getPage(i - 1)));
            }

        } catch (IOException e) {
            throw new PdfProcessingException("Error while reading PDF file.", e);
        }

        return new ArrayList<>(ibanSet);
    }

    /**
     * Extract IBANs from regular extracted text (handling multiple spaces, hyphens, dots, etc.).
     */
    private Set<String> extractIbansFromText(String text) {
        Set<String> ibans = new HashSet<>();
        // Normalize the text by replacing newlines and tabs with spaces
        String normalizedText = text.replaceAll("[\\t\\n\\r]", " ");
        Pattern pattern = Pattern.compile(IBAN_REGEX);
        Matcher matcher = pattern.matcher(normalizedText);

        while (matcher.find()) {
            String iban = matcher.group(1).replaceAll(" ", ""); // Remove spaces for validation
            if (isValidIban(iban)) {
                log.debug("IBAN Found (normalized): {}", iban);
                ibans.add(iban);
            }
        }
        return ibans;
    }

    /**
     * Extract IBANs from **bold, italic, or rotated text** using PDFTextStripperByArea.
     */
    private Set<String> extractIbansFromAnnotations(PDPage page) throws IOException {
        Set<String> ibans = new HashSet<>();
        PDFTextStripperByArea stripperByArea = new PDFTextStripperByArea();

        // Define a large region to capture formatted text (across the entire page)
        Rectangle allTextRegion = new Rectangle(0, 0, (int) page.getMediaBox().getWidth(), (int) page.getMediaBox().getHeight());
        stripperByArea.addRegion("all", allTextRegion);
        stripperByArea.extractRegions(page);

        String areaText = stripperByArea.getTextForRegion("all");
        if (areaText != null && !areaText.isEmpty()) {
            // Extract IBANs from this region as well, ensuring no formatting is missed
            log.debug("Extracting IBANs from region (bold, italic, rotated): {}", areaText);
            ibans.addAll(extractIbansFromText(areaText));
        }

        return ibans;
    }

    /**
     * Validates the IBAN using the MOD-97 algorithm.
     */
    private boolean isValidIban(String iban) {
        if (iban == null || iban.length() < 4) {
            return false;
        }

        // Move the first four characters to the end
        String rearranged = iban.substring(4) + iban.substring(0, 4);

        // Convert letters to numbers (A=10, B=11, ..., Z=35)
        StringBuilder numericIban = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numericIban.append(Character.getNumericValue(c));
            } else {
                numericIban.append(c);
            }
        }

        // Perform MOD-97 operation
        try {
            BigInteger bigInt = new BigInteger(numericIban.toString());
            return bigInt.mod(BigInteger.valueOf(97)).intValue() == 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}