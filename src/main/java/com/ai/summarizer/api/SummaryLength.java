package com.ai.summarizer.api;

public enum SummaryLength {
  SHORT,   // ~2 sentences
  MEDIUM,  // ~5 sentences
  LONG;    // ~8 sentences

  public int toSentenceCap() {
    return switch (this) {
      case SHORT -> 2;
      case MEDIUM -> 5;
      case LONG -> 8;
    };
  }
}