package com.ai.summarizer.service;

import com.ai.summarizer.api.SummarizeRequest;
import com.ai.summarizer.api.SummarizeResponse;

public interface SummarizerService {
    SummarizeResponse summarize(SummarizeRequest request);
}
