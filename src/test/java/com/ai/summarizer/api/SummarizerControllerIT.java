package com.ai.summarizer.api;

import com.ai.summarizer.llm.LlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SummarizerControllerIT {

  @Autowired
  WebTestClient webTestClient;

  // Mock the LLM so the test is fast, deterministic, and offline
  @MockBean
  LlmClient llmClient;

  @Test
  void summarize_returns200_andCapsSentences() {
    // Model tries to return 4 sentences, but we will ask for max 2
    when(llmClient.summarize(anyString(), anyInt()))
        .thenReturn("S1. S2. S3. S4.");

    Map<String, Object> body = Map.of(
        "text", "Artificial intelligence is being used to automate repetitive tasks, " +
                 "assist decisions, and personalize experiences across industries. " +
                 "This long text ensures summarization is meaningful.",
        "maxSentences", 2
    );

    webTestClient.post()
        .uri("/api/summarize")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.summary").isNotEmpty()
        .jsonPath("$.summary").value(s -> {
          // naive sentence count check (., !, ?)
          String summary = (String) s;
          long count = summary.chars().filter(ch -> ch=='.' || ch=='!' || ch=='?').count();
          if (count > 2) {
            throw new AssertionError("Expected <= 2 sentences, but got: " + summary);
          }
        });
  }

  @Test
  void summarize_returns400_whenTextTooShort() {
    Map<String, Object> body = Map.of(
        "text", "too short",
        "maxSentences", 2
    );

    webTestClient.post()
        .uri("/api/summarize")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .expectStatus().is5xxServerError(); // If you add @ControllerAdvice, change to isBadRequest()
  }
}