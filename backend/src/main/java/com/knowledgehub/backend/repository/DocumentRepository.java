
package com.knowledgehub.backend.repository;

import com.knowledgehub.backend.entities.KnowledgeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DocumentRepository
        extends MongoRepository<KnowledgeDocument, String> {
}
