package com.company.invoice_scanner_service.service.pdf;

import com.company.invoice_scanner_service.exception.InvalidUrlException;
import com.company.invoice_scanner_service.exception.PdfNotFoundException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PdfDownloadServiceTest {

    private static PdfDownloadService pdfDownloadService;
    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setup() {
        // Start WireMock server for mocking HTTP responses
        wireMockServer = new WireMockServer(8081);
        wireMockServer.start();

        pdfDownloadService = new PdfDownloadService();
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testDownloadPdf_ValidUrl_ShouldReturnPdfFile() {
        // Mock a successful PDF response
        wireMockServer.stubFor(get(urlEqualTo("/valid.pdf"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/pdf")
                        .withBody(new byte[]{1, 2, 3, 4, 5}))); // Mocked PDF content

        String validUrl = "http://localhost:8081/valid.pdf";
        List<File> downloadedFiles = pdfDownloadService.downloadPdfs(validUrl);

        assertNotNull(downloadedFiles);
        assertFalse(downloadedFiles.isEmpty());
        assertTrue(downloadedFiles.getFirst().exists());

        // Clean up temporary file
        downloadedFiles.getFirst().delete();
    }

    @Test
    void testDownloadPdf_InvalidUrl_ShouldThrowInvalidUrlException() {
        String invalidUrl = "invalid-url";

        Exception exception = assertThrows(InvalidUrlException.class, () -> pdfDownloadService.downloadPdfs(invalidUrl));
        assertEquals("Invalid URL format: invalid-url", exception.getMessage());
    }

    @Test
    void testDownloadPdf_UrlNotReturningPdf_ShouldThrowPdfNotFoundException() {
        // Mock a response with non-PDF content type
        wireMockServer.stubFor(get(urlEqualTo("/not-a-pdf"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "text/html")
                        .withBody("<html>Not a PDF</html>")));

        String nonPdfUrl = "http://localhost:8081/not-a-pdf";

        Exception exception = assertThrows(PdfNotFoundException.class, () -> pdfDownloadService.downloadPdfs(nonPdfUrl));
        assertEquals("No PDFs found at the given URL.", exception.getMessage());
    }

    @Test
    void testDownloadPdf_UrlReturns404_ShouldThrowPdfNotFoundException() {
        // Mock a 404 response
        wireMockServer.stubFor(get(urlEqualTo("/missing.pdf"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())));

        String missingPdfUrl = "http://localhost:8081/missing.pdf";

        Exception exception = assertThrows(PdfNotFoundException.class, () -> pdfDownloadService.downloadPdfs(missingPdfUrl));
        assertEquals("No PDFs found at the given URL.", exception.getMessage());
    }

    @Test
    void testDownloadPdf_ServerError_ShouldThrowPdfNotFoundException() {
        // Mock a 500 Internal Server Error
        wireMockServer.stubFor(get(urlEqualTo("/server-error.pdf"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())));

        String serverErrorUrl = "http://localhost:8081/server-error.pdf";

        Exception exception = assertThrows(PdfNotFoundException.class, () -> pdfDownloadService.downloadPdfs(serverErrorUrl));
        assertEquals("No PDFs found at the given URL.", exception.getMessage());
    }
}