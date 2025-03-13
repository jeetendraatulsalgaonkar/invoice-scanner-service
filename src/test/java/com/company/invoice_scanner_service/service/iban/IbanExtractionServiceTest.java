package com.company.invoice_scanner_service.service.iban;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IbanExtractionServiceTest {
    private IbanExtractionService ibanExtractionService;

    private File validPdf;
    private File invalidPdf;

    @BeforeEach
    void setUp() throws IOException {
        ibanExtractionService = new IbanExtractionService();

        // Create a valid PDF with IBANs
        validPdf = createTestPdf("Valid IBAN: DE44 5001 0517 5407 3249 31");

        // Create an invalid (corrupt) PDF file
        invalidPdf = File.createTempFile("corrupt_pdf", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(invalidPdf)) {
            fos.write(new byte[]{0, 1, 2, 3}); // Writing garbage data
        }
    }

    @AfterEach
    void tearDown() {
        if (validPdf != null) validPdf.delete();
        if (invalidPdf != null) invalidPdf.delete();
    }

    @Test
    void testExtractIbans_ValidPdf() throws IOException {
        List<String> ibans = ibanExtractionService.extractIbans(validPdf);
        assertFalse(ibans.isEmpty());
        assertEquals("DE44500105175407324931", ibans.get(0)); // Ensure IBAN is correctly extracted
    }

    @Test
    void testExtractIbans_NoIbansPdf() throws IOException {
        File noIbanPdf = createTestPdf("This document contains no IBANs.");
        List<String> ibans = ibanExtractionService.extractIbans(noIbanPdf);
        assertTrue(ibans.isEmpty());
        noIbanPdf.delete();
    }

    private File createTestPdf(String text) throws IOException {
        File pdfFile = File.createTempFile("test_pdf", ".pdf");
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12); // ✅ Set font before writing text
                contentStream.beginText();
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(text);
                contentStream.endText();
            }

            document.save(pdfFile);
        }
        return pdfFile;
    }

}