package com.knowledgehub.backend.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("!render")
public class LocalDocumentEmbeddingService {

    private final EmbeddingModel embeddingModel;

    public LocalDocumentEmbeddingService(
            @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public List<float[]> generateEmbeddings(List<String> chunks) {
        return chunks.stream()
                .map(chunk -> embeddingModel.embed(chunk))
                .toList();
    }
}