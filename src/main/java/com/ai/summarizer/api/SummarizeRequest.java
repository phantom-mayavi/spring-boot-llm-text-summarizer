package com.ai.summarizer.api;

import jakarta.validation.constraints.NotBlank;

public record SummarizeRequest(
        @NotBlank String text,
        Integer maxSentences
) {}