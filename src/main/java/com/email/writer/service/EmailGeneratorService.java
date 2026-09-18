package com.email.writer.service;

import com.email.writer.EmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class EmailGeneratorService
{
    private final WebClient webClient;
    private final String apiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder,
                                 @Value("${gemini.api.url}") String baseUrl,
                                 @Value("${gemini.api.key}") String geminiApiKey)
    {
        this.apiKey = geminiApiKey;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public String generateEmailReply(EmailRequest emailRequest)
    {
        // 1. Build prompt
        String prompt = buildPrompt(emailRequest);

        // 2. Prepare correct Gemini API JSON body structure
        String requestBody = String.format("""
                {
                  "contents": [
                    {
                      "parts": [
                        {
                          "text": "%s"
                        }
                      ]
                    }
                  ]
                }""", prompt);

        // 3. Send request to the correct Gemini model endpoint with gemini-3.5-flash
        String response = webClient.post()
                .uri("/v1beta/models/gemini-3.5-flash:generateContent")
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 4. Extract response content
        return extractResponseContent(response);
    }

    private String extractResponseContent(String response)
    {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // Navigate through Gemini's standard response path: candidates -> content -> parts -> text
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + response, e);
        }
    }

    private String buildPrompt(EmailRequest emailRequest)
    {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email: ");
        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty())
        {
            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone. ");
        }
        prompt.append("Original Email:\n").append(emailRequest.getEmailContent());

        return prompt.toString();
    }
}