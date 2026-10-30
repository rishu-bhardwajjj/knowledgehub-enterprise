package com.knowledgehub.backend.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final ChatClient chatClient;
    private final DocumentRetrievalService retrievalService;

    public RagService(
            ChatClient.Builder builder,
            DocumentRetrievalService retrievalService) {
        this.chatClient = builder.build();
        this.retrievalService = retrievalService;
    }

    public String answerQuestion(String question) {
        List<Document> relevantChunks =
                retrievalService.retrieveRelevantChunks(question);

        if (relevantChunks.isEmpty()) {
            return "I couldn't find relevant information in the indexed documents.";
        }

        String context = relevantChunks.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        return chatClient.prompt()
                .system("""
                        You are KnowledgeHub, a document question-answering assistant.
                        Answer the user's question using only the supplied document context.
                        If the context does not contain the answer, say that the documents
                        do not provide sufficient information.
                        Do not invent facts.
                        """)
                .user("""
                        Document context:
                        %s

                        Question:
                        %s
                        """.formatted(context, question))
                .call()
                .content();
    }
}