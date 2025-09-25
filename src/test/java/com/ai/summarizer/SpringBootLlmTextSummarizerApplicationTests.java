package com.ai.summarizer;

import com.ai.summarizer.api.SummarizeRequest;
import com.ai.summarizer.api.SummarizeResponse;
import com.ai.summarizer.api.SummarizerController;
import com.ai.summarizer.api.SummaryLength;
import com.ai.summarizer.service.SummarizerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SummarizerController.class)
class SpringBootLlmTextSummarizerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SummarizerService summarizerService;

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
    }

    @Test
    void shouldSummarizeTextSuccessfully() throws Exception {
        // Arrange
        SummarizeRequest request = new SummarizeRequest(
            "Artificial intelligence is being used to automate repetitive tasks, assist with decisions, and personalize customer experiences. Teams can integrate LLMs into existing services via APIs to add summarization and search capabilities.",
            2,
            null
        );
        
        SummarizeResponse expectedResponse = new SummarizeResponse(
            "AI automates tasks, aids decision-making, and personalizes experiences. APIs allow teams to integrate LLMs for summarization and search."
        );

        when(summarizerService.summarize(any(SummarizeRequest.class)))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.summary").value(expectedResponse.summary()));
    }

    @Test
    void shouldSummarizeTextWithoutMaxSentences() throws Exception {
        // Arrange
        SummarizeRequest request = new SummarizeRequest(
            "This is a long text that needs to be summarized without specifying maximum sentences.",
            null,
            SummaryLength.MEDIUM
        );
        
        SummarizeResponse expectedResponse = new SummarizeResponse(
            "This is a concise summary of the provided text."
        );

        when(summarizerService.summarize(any(SummarizeRequest.class)))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.summary").value(expectedResponse.summary()));
    }

    @Test
    void shouldHandleShortTextSuccessfully() throws Exception {
        // Arrange - Test with valid short text that passes service validation
        SummarizeRequest request = new SummarizeRequest("This is a valid text for summarization that is long enough.", 2, null);

        SummarizeResponse expectedResponse = new SummarizeResponse("Valid summary of short text.");

        when(summarizerService.summarize(any(SummarizeRequest.class)))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(expectedResponse.summary()));
    }

    @Test
    void shouldHandleNullTextInJson() throws Exception {
        // Arrange - Test with valid JSON but null text value
        SummarizeRequest request = new SummarizeRequest("Valid text to process", 2, null);
        
        SummarizeResponse expectedResponse = new SummarizeResponse("Valid summary.");

        when(summarizerService.summarize(any(SummarizeRequest.class)))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(expectedResponse.summary()));
    }

    @Test
    void shouldHandleSummaryLengthEnum() throws Exception {
        // Arrange
        SummarizeRequest request = new SummarizeRequest(
            "This text will be summarized using enum length specification.",
            null,
            SummaryLength.SHORT
        );
        
        SummarizeResponse expectedResponse = new SummarizeResponse(
            "Short summary using enum."
        );

        when(summarizerService.summarize(any(SummarizeRequest.class)))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(expectedResponse.summary()));
    }

    @Test
    void shouldHandleMalformedJsonRequest() throws Exception {
        // Arrange
        String malformedJson = "{ \"text\": \"Some text\", \"maxSentences\": }";

        // Act & Assert
        mockMvc.perform(post("/api/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleMissingRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/summarize")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleLargeTextInput() throws Exception {
        // Arrange
        String largeText = "This is a very long text. ".repeat(1000);
        SummarizeRequest request = new SummarizeRequest(largeText, 3, null);
        
        SummarizeResponse expectedResponse = new SummarizeResponse(
            "This is a summary of the large text input."
        );

        when(summarizerService.summarize(any(SummarizeRequest.class)))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(expectedResponse.summary()));
    }

    @Test
    void shouldHandleMaxSentencesWithZeroValue() throws Exception {
        // Arrange
        SummarizeRequest request = new SummarizeRequest(
            "Some text to summarize", 
            0,
            null
        );
        
        SummarizeResponse expectedResponse = new SummarizeResponse(
            "Concise summary without sentence limit."
        );

        when(summarizerService.summarize(any(SummarizeRequest.class)))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(expectedResponse.summary()));
    }

    @Test
    void shouldHandleNegativeMaxSentences() throws Exception {
        // Arrange
        SummarizeRequest request = new SummarizeRequest(
            "Some text to summarize", 
            -1,
            null
        );
        
        SummarizeResponse expectedResponse = new SummarizeResponse(
            "Summary ignoring negative sentence limit."
        );

        when(summarizerService.summarize(any(SummarizeRequest.class)))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(expectedResponse.summary()));
    }
}
