package com.knowledgehub.backend.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentEmbeddingService {

    private final EmbeddingModel embeddingModel;

    public DocumentEmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public List<float[]> generateEmbeddings(List<String> chunks) {
        return chunks.stream()
                .map(chunk -> embeddingModel.embed(chunk))
                .toList();
    }
}