package com.knowledgehub.backend.controllers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.knowledgehub.backend.service.DocumentTextExtractionService;
import org.springframework.web.bind.annotation.PathVariable;
import com.knowledgehub.backend.service.RagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
@RestController
@RequestMapping("/api/ai")
public class AiTestController {

    private final ChatClient chatClient;
    private final DocumentTextExtractionService textExtractionService;
    private final RagService ragService;

    public AiTestController(
            ChatClient.Builder builder,
            DocumentTextExtractionService textExtractionService,
            RagService ragService) {
        this.chatClient = builder.build();
        this.textExtractionService = textExtractionService;
        this.ragService = ragService;
    }

    @GetMapping("/test")
    public String testAi() {
        return chatClient.prompt()
                .user("Explain RAG in one simple sentence.")
                .call()
                .content();
    }
    @GetMapping("/extract/{filename}")
    public String extractText(@PathVariable String filename)
            throws java.io.IOException {
        return textExtractionService.extractText(filename);
    }
    public record AskRequest(String question) {}

    @PostMapping("/ask")
    public String askQuestion(@RequestBody AskRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            throw new IllegalArgumentException("Question cannot be empty.");
        }

        return ragService.answerQuestion(request.question());
    }
}