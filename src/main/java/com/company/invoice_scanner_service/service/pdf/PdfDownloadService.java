package com.company.invoice_scanner_service.service.pdf;

import com.company.invoice_scanner_service.exception.InvalidUrlException;
import com.company.invoice_scanner_service.exception.PdfNotFoundException;
import com.company.invoice_scanner_service.exception.PdfProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PdfDownloadService {
    
    /**
     * Downloads PDFs from the given URL.
     */
    public List<File> downloadPdfs(String pdfUrl) {
        validateUrl(pdfUrl);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(pdfUrl).openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() != HttpStatus.OK.value()) {
                throw new PdfNotFoundException("No PDFs found at the given URL.");
            }

            File pdfFile = savePdfFromStream(connection.getInputStream());
            return List.of(pdfFile);

        } catch (IOException e) {
            log.error("Error downloading PDF from URL: {}", pdfUrl, e);
            throw new PdfProcessingException("Failed to process downloaded PDFs from the provided URL.");
        }
    }

    /**
     * Validates URL format.
     */
    private void validateUrl(String url) {
        if (!StringUtils.hasText(url) || !url.startsWith("http")) {
            throw new InvalidUrlException("Invalid URL format: " + url);
        }
    }

    /**
     * Saves PDF input stream to a local file.
     */
    private File savePdfFromStream(InputStream inputStream) throws IOException {
        File tempFile = File.createTempFile("pdf_download_", ".pdf");
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }
}
