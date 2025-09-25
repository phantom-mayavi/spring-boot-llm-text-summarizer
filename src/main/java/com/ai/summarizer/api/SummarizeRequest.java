package com.ai.summarizer.api;

import jakarta.validation.constraints.NotBlank;

public record SummarizeRequest(
    @NotBlank String text,
    Integer maxSentences,        // optional explicit cap
    SummaryLength length         // optional semantic length
) {}