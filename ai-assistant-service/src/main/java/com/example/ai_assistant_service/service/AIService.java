package com.example.ai_assistant_service.service;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.ai_assistant_service.dto.ChatResponse;

import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private final RestClient restClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model}")
    private String model;

    public AIService(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public ChatResponse getAIResponse(String message) {

        String prompt = """
                You are EduManage AI Assistant, a helpful assistant
                for a Student Management System.

                Help students with:
                - Java and programming questions
                - Study plans and learning
                - Academic guidance
                - General educational questions

                Explain answers in simple language.
                Do not invent student marks, attendance or personal records.
                If asked for private academic information that has not been
                provided to you, explain that you cannot access it yet.

                User question:
                """ + message;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        try {
            JsonNode response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new RuntimeException("Empty response received from Gemini");
            }

            JsonNode textNode = response.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode() || textNode.isNull()) {
                throw new RuntimeException(
                        "Gemini did not return a text response. Check the API response."
                );
            }

            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setReply(textNode.asText());

            return chatResponse;

        } catch (RestClientException exception) {

            if (exception instanceof
                    org.springframework.web.client.HttpServerErrorException serverError
                    && (serverError.getStatusCode().value() == 503
                    || serverError.getStatusCode().value() == 429)) {

                throw new RuntimeException(
                        "Gemini is temporarily busy or rate-limited. Please try again shortly."
                );
            }

            throw new RuntimeException(
                    "Gemini API communication failed: "
                            + exception.getMessage(),
                    exception
            );
        }
    }
}
