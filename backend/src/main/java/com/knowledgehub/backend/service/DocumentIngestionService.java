
package com.knowledgehub.backend.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DocumentIngestionService {

    private final DocumentTextExtractionService textExtractionService;
    private final DocumentChunkingService chunkingService;
    private final VectorStore vectorStore;

    public DocumentIngestionService(
            DocumentTextExtractionService textExtractionService,
            DocumentChunkingService chunkingService,
            VectorStore vectorStore) {
        this.textExtractionService = textExtractionService;
        this.chunkingService = chunkingService;
        this.vectorStore = vectorStore;
    }

    public int ingestDocument(String storedFilename) throws IOException {
        String text = textExtractionService.extractText(storedFilename);

        List<String> chunks = chunkingService.chunkText(text);

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "No readable text was found in the document.");
        }

        List<Document> documents = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            Map<String, Object> metadata = Map.of(
                    "storedFilename", storedFilename,
                    "chunkIndex", i,
                    "source", storedFilename
            );

            documents.add(new Document(chunks.get(i), metadata));
        }

        vectorStore.add(documents);

        return chunks.size();
    }
}
