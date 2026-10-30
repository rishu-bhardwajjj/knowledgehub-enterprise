
package com.knowledgehub.backend.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mongodb.atlas.MongoDBAtlasVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class VectorStoreConfig {

    @Value("${app.mongodb.metadata-uri}")
    private String metadataMongoUri;

    @Value("${app.mongodb.vector-uri}")
    private String vectorMongoUri;

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
    public VectorStore vectorStore(
            MongoTemplate vectorMongoTemplate,
            EmbeddingModel embeddingModel) {

        return MongoDBAtlasVectorStore.builder(
                        vectorMongoTemplate, embeddingModel)
                .collectionName("vector_store")
                .vectorIndexName("vector_index")
                .initializeSchema(true)
                .build();
    }
}
