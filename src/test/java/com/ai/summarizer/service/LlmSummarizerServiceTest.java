package com.ai.summarizer.service;

import com.ai.summarizer.api.SummarizeRequest;
import com.ai.summarizer.api.SummarizeResponse;
import com.ai.summarizer.api.SummaryLength;
import com.ai.summarizer.llm.LlmClient;
import com.ai.summarizer.service.LlmSummarizerService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmSummarizerServiceTest {

  @Mock
  LlmClient llmClient;

  @InjectMocks
  LlmSummarizerService service;

  @Test
  void summarize_capsByMaxSentences_evenIfModelIsVerbose() {
    // Model returns 4 sentences, but request asks for max 2
    Mockito.when(llmClient.summarize(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt()))
        .thenReturn("S1. S2. S3. S4.");

    SummarizeRequest req = new SummarizeRequest(
        "This is a sufficiently long input so it should be summarized into fewer sentences. " +
        "Adding more content to exceed minimum length requirement and provide punctuation.",
        2,   // maxSentences
        null // length enum not used here
    );

    SummarizeResponse resp = service.summarize(req);

    // Expect at most 2 sentences
    int sentences = countSentences(resp.summary());
    Assertions.assertThat(sentences).isLessThanOrEqualTo(2);
  }

  @Test
  void summarize_usesEnumLength_whenMaxSentencesNotProvided() {
    // Model returns 3 sentences; SHORT should cap to ~2
    Mockito.when(llmClient.summarize(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt()))
        .thenReturn("First. Second. Third.");

    SummarizeRequest req = new SummarizeRequest(
        "Artificial intelligence is being used to automate repetitive tasks, assist decisions, " +
        "and personalize experiences across industries. This text exists to provide enough length " +
        "and punctuation for a meaningful summary output.",
        null,                 // no explicit max
        SummaryLength.SHORT   // ~2 sentences
    );

    SummarizeResponse resp = service.summarize(req);
    Assertions.assertThat(countSentences(resp.summary())).isLessThanOrEqualTo(2);
  }

  @Test
  void summarize_prefersMaxSentences_overEnumLength() {
    // Even though LONG (~8), explicit max=3 should win
    Mockito.when(llmClient.summarize(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt()))
        .thenReturn("A. B. C. D. E.");

    SummarizeRequest req = new SummarizeRequest(
        "This is a long enough input to validate precedence rules between explicit max and enum. " +
        "The service should respect the explicit numeric cap when both are provided.",
        3,
        SummaryLength.LONG
    );

    SummarizeResponse resp = service.summarize(req);
    Assertions.assertThat(countSentences(resp.summary())).isLessThanOrEqualTo(3);
  }

  @Test
  void summarize_throwsOnTooShortInput() {
    SummarizeRequest req = new SummarizeRequest(
        "too short", // < 20 chars
        2,
        null
    );
    Assertions.assertThatThrownBy(() -> service.summarize(req))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("too short");
  }

  @Test
  void summarize_throwsWhenModelReturnsEmpty() {
    Mockito.when(llmClient.summarize(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenReturn("");

    SummarizeRequest req = new SummarizeRequest(
        "This is a sufficiently long input to exercise the empty-output guardrail. " +
        "If the model returns nothing, the service should throw.",
        2,
        null
    );

    Assertions.assertThatThrownBy(() -> service.summarize(req))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Empty summary");
  }

  // --- helpers ---

  private int countSentences(String text) {
    int count = 0;
    for (char c : text.toCharArray()) {
      if (c == '.' || c == '!' || c == '?') count++;
    }
    return count == 0 && !text.isBlank() ? 1 : count;
  }
}