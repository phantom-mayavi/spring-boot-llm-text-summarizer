package com.ai.summarizer.llm;

public interface LlmClient {
    String summarize(String text, Integer maxSentences);
}
