
        package com.knowledgehub.backend.service;

import com.knowledgehub.backend.dto.CreateDocumentRequest;
import com.knowledgehub.backend.entities.KnowledgeDocument;
import com.knowledgehub.backend.repository.DocumentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    public List<KnowledgeDocument> getAll() {
        return repository.findAll();
    }

    public KnowledgeDocument getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Document not found"));
    }

    public KnowledgeDocument create(CreateDocumentRequest request) {

        KnowledgeDocument document = new KnowledgeDocument(
                request.title().trim(),
                request.description(),
                request.category().trim(),
                request.uploadedBy().trim()
        );

        return repository.save(document);
    }

    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Document not found");
        }

        repository.deleteById(id);
    }

    public KnowledgeDocument update(
            String id,
            CreateDocumentRequest request) {


        KnowledgeDocument document = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Document not found"));

        document.updateDetails(
                request.title().trim(),
                document.getContent(),
                request.description(),
                request.category().trim()
        );

        return repository.save(document);
    }
    public KnowledgeDocument createUploadedDocument(
            String originalFilename,
            String storedFilename,
            String contentType,
            long fileSize) {

        KnowledgeDocument document = new KnowledgeDocument();

        document.setTitle(originalFilename);
        document.setDescription("Uploaded file: " + originalFilename);
        document.setCategory("Uploaded Documents");
        document.setUploadedBy("User");

        document.setOriginalFilename(originalFilename);
        document.setStoredFilename(storedFilename);
        document.setContentType(contentType);
        document.setFileSize(fileSize);

        return repository.save(document);
    }
}
