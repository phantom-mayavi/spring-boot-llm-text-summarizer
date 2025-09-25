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
    public String summarize(String text, Integer maxSentences) {
        String limit = (maxSentences != null && maxSentences > 0)
                ? "in at most " + maxSentences + " sentences"
                : "concisely";

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text",
                                "Summarize the following text " + limit + " without adding new facts:\n\n" + text))
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

        // If the API returned an error, show it clearly
        if (resp != null && resp.containsKey("error")) {
            Map<String, Object> err = (Map<String, Object>) resp.get("error");
            Object code = err.get("code");
            Object message = err.get("message");
            throw new RuntimeException("Gemini API error (" + code + "): " + message);
        }

        // Extract: candidates[0].content.parts[0].text
        var candidates = resp == null ? null : (List<Map<String, Object>>) resp.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Gemini returned no candidates (check API key/model).");
        }
        var content = (Map<String, Object>) candidates.get(0).get("content");
        var parts = content == null ? null : (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            throw new RuntimeException("Gemini returned empty parts (check request).");
        }
        return (String) parts.get(0).get("text");
    }


}
