package com.knowledgehub.backend.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mongodb.atlas.MongoDBAtlasVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class VectorStoreConfig {

    @Value("${app.mongodb.metadata-uri}")
    private String metadataMongoUri;

    @Value("${app.mongodb.vector-uri}")
    private String vectorMongoUri;

    @Value("${app.mongodb.vector-collection:vector_store}")
    private String vectorCollection;

    @Bean(name = "mongoTemplate")
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(
                new SimpleMongoClientDatabaseFactory(metadataMongoUri)
        );
    }

    @Bean
    public MongoTemplate vectorMongoTemplate() {
        return new MongoTemplate(
                new SimpleMongoClientDatabaseFactory(vectorMongoUri)
        );
    }

    @Bean
    @Profile("render")
    public VectorStore vectorStore(
            MongoTemplate vectorMongoTemplate,
            @Qualifier("googleGenAiTextEmbedding") EmbeddingModel embeddingModel) {

        return MongoDBAtlasVectorStore.builder(
                        vectorMongoTemplate, embeddingModel)
                .collectionName(vectorCollection)
                .vectorIndexName("vector_index")
                .initializeSchema(false)
                .build();
    }

    @Bean
    @Profile("!render")
    public VectorStore localVectorStore(
            MongoTemplate vectorMongoTemplate,
            @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {

        return MongoDBAtlasVectorStore.builder(
                        vectorMongoTemplate, embeddingModel)
                .collectionName(vectorCollection)
                .vectorIndexName("vector_index")
                .initializeSchema(false)
                .build();
    }
}