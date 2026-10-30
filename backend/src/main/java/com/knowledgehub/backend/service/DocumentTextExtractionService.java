package com.knowledgehub.backend.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DocumentTextExtractionService {

    private final Tika tika = new Tika();

    private final Path uploadDirectory =
            Paths.get("uploads").toAbsolutePath().normalize();

    public String extractText(String storedFilename) throws IOException {
        Path filePath = uploadDirectory.resolve(storedFilename).normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        if (!Files.isRegularFile(filePath)) {
            throw new IOException("Uploaded file was not found.");
        }

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            return tika.parseToString(inputStream);
        } catch (TikaException e) {
            throw new RuntimeException(e);
        }
    }
}