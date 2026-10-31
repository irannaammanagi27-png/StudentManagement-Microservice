package com.example.Marks.Service.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class StudentEmailClient {

    private final RestClient restClient;

    public StudentEmailClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }

    public StudentResponse getStudent(
            Long studentId,
            String authorization) {

        return restClient.get()
                .uri("/api/students/{id}", studentId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(StudentResponse.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StudentResponse(
            Long id,
            String name,
            String email,
            String department,
            Integer age
    ) {
    }
}