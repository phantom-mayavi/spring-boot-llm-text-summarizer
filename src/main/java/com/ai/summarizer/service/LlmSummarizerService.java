package com.ai.summarizer.service;

import com.ai.summarizer.api.SummarizeRequest;
import com.ai.summarizer.api.SummarizeResponse;
import com.ai.summarizer.llm.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LlmSummarizerService implements SummarizerService {
    private static final int DEFAULT_CAP = 5; // safe default
    private final LlmClient llmClient;

    @Override
    public SummarizeResponse summarize(SummarizeRequest req) {
        String input = req.text().trim();
        if (input.length() < 20) {
            throw new IllegalArgumentException("Input text is too short to summarize.");
        }

        int cap = resolveCap(req);

        // 1) Ask the LLM explicitly to be concise — never expand
        String llmOut = llmClient.summarize(concisePromptPrefix(cap) + input, cap);
        if (llmOut == null || llmOut.isBlank()) {
            throw new RuntimeException("Empty summary from LLM");
        }

        // 2) Guardrail: never return something longer than input
        String trimmed = hardCapBySentences(llmOut, cap);
        if (trimmed.length() >= input.length()) {
            // If LLM “expanded”, fall back to head summary (very rare with cap + prompt)
            trimmed = hardCapBySentences(input, cap);
        }

        return new SummarizeResponse(trimmed.trim());
    }

    private int resolveCap(SummarizeRequest req) {
        if (req.maxSentences() != null && req.maxSentences() > 0) {
            return Math.min(req.maxSentences(), 12); // sanity ceiling
        }
        if (req.length() != null) {
            return req.length().toSentenceCap();
        }
        return DEFAULT_CAP;
    }

    // Keep the instruction minimal; LlmClient will pass this text as the "user" message.
    private String concisePromptPrefix(int cap) {
        return "Summarize the following text in a strictly concise way. " +
                "Do not expand, rephrase extensively, or add new facts. " +
                "Focus only on the key points. Limit to a maximum of " + cap + " sentences.\n\n";
    }

    /** Very simple sentence capper (period/question/exclamation). */
    private String hardCapBySentences(String text, int cap) {
        List<String> out = new ArrayList<>(cap);
        int start = 0, count = 0;
        for (int i = 0; i < text.length() && count < cap; i++) {
            char c = text.charAt(i);
            boolean end = (c == '.' || c == '!' || c == '?');
            if (end) {
                out.add(text.substring(start, i + 1).trim());
                count++;
                start = Math.min(i + 1, text.length());
            }
        }
        // If the model returned no punctuation, take a hard substring
        if (out.isEmpty()) {
            return text.length() > 500 ? text.substring(0, 500) : text;
        }
        return String.join(" ", out);
    }
}
