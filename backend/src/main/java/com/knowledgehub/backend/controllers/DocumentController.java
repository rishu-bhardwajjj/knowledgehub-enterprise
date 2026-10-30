
package com.knowledgehub.backend.controllers;

import java.util.List;

import com.knowledgehub.backend.dto.CreateDocumentRequest;
import com.knowledgehub.backend.entities.KnowledgeDocument;
import com.knowledgehub.backend.service.DocumentService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.knowledgehub.backend.service.FileStorageService;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import com.knowledgehub.backend.service.DocumentIngestionService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {

    private final DocumentService service;
    private final DocumentIngestionService ingestionService;
    private final FileStorageService fileStorageService;
    public DocumentController(
            DocumentService service,
            FileStorageService fileStorageService,
            DocumentIngestionService ingestionService) {
        this.service = service;
        this.fileStorageService = fileStorageService;
        this.ingestionService = ingestionService;
    }

    @GetMapping
    public List<KnowledgeDocument> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public KnowledgeDocument getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeDocument create(
            @Valid @RequestBody CreateDocumentRequest request) {
        return service.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
    @PutMapping("/{id}")
    public KnowledgeDocument update(
            @PathVariable String id,
            @Valid @RequestBody CreateDocumentRequest request) {
        return service.update(id, request);
    }
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Please select a file to upload.");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null ||
                !(originalFilename.toLowerCase().endsWith(".pdf")
                        || originalFilename.toLowerCase().endsWith(".docx")
                        || originalFilename.toLowerCase().endsWith(".txt"))) {
            return ResponseEntity.badRequest()
                    .body("Only PDF, DOCX, and TXT files are allowed.");
        }

        try {
            String storedFilename = fileStorageService.store(file);

            service.createUploadedDocument(
                    originalFilename,
                    storedFilename,
                    file.getContentType(),
                    file.getSize()
            );
            int chunksCreated = ingestionService.ingestDocument(storedFilename);
            return ResponseEntity.ok(
                    "File uploaded and indexed successfully. Chunks created: "
                            + chunksCreated
            );

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to store the uploaded file.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
