package com.ai.summarizer.llm;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeminiClient implements LlmClient {
    private final WebClient webClient;

    @Value("${gemini.api-key}")
    private String apiKey;
    @Value("${gemini.model}")
    private String model;
    @Value("${llm.temperature}")
    private double temperature;
    @Value("${llm.max-tokens}")
    private int maxTokens;

    // com/ai/summarizer/llm/GeminiClient.java
    @Override
    public String summarize(String userContent, Integer ignoredCap) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userContent))
                )),
                "generationConfig", Map.of(
                        "temperature", temperature,
                        "maxOutputTokens", maxTokens
                )
        );

        Map<?, ?> resp = webClient.post()
                .uri(u -> u.path("/v1beta/models/{model}:generateContent")
                        .queryParam("key", apiKey)
                        .build(model))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (resp != null && resp.containsKey("error")) {
            Map<String, Object> err = (Map<String, Object>) resp.get("error");
            throw new RuntimeException("Gemini API error (" + err.get("code") + "): " + err.get("message"));
        }
        var candidates = resp == null ? null : (List<Map<String, Object>>) resp.get("candidates");
        if (candidates == null || candidates.isEmpty()) return null;
        var content = (Map<String, Object>) candidates.get(0).get("content");
        var parts = content == null ? null : (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) return null;
        return (String) parts.get(0).get("text");
    }


}
