package com.knowledgehub.backend.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("render")
public class DocumentEmbeddingService {

    private final EmbeddingModel embeddingModel;

    public DocumentEmbeddingService(
            @Qualifier("googleGenAiTextEmbedding") EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public List<float[]> generateEmbeddings(List<String> chunks) {
        return chunks.stream()
                .map(chunk -> embeddingModel.embed(chunk))
                .toList();
    }
}