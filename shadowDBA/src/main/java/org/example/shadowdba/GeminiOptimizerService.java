package org.example.shadowdba;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiOptimizerService {

    private static final Logger log = LoggerFactory.getLogger(GeminiOptimizerService.class);

    private final RestClient restClient;
    private final ShadowDbaProperties properties;
    private final ObjectMapper objectMapper;

    // We use the Gemini 1.5 Pro model for expert DBA analysis
    //private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
   // private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent";
    private static final String PROMPT_TEMPLATE =
            "You are an expert Database Administrator. The following raw SQL query took %d milliseconds to execute. " +
                    "Analyze it and provide a concise, optimized version or suggest specific database indexes to improve performance. " +
                    "Keep the answer under 150 words.\n\nQuery: %s";

    public GeminiOptimizerService(ShadowDbaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    @Async // This ensures the API call happens in the background without slowing down the user's app
    public void analyzeQuery(String sql, long executionTimeMs) {
        if (properties.getGeminiApiKey() == null || properties.getGeminiApiKey().isBlank()) {
            log.warn("[shadowDBA] Gemini API Key is missing. Please add shadow-dba.gemini-api-key to your application.properties.");
            return;
        }

        try {
            String prompt = String.format(PROMPT_TEMPLATE, executionTimeMs, sql);

            // Build the JSON payload expected by Gemini using Maps (safe from SQL quotes breaking JSON)
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            // Make the POST request to Google's servers
            String responseJson = restClient.post()
                    .uri(GEMINI_URL + "?key=" + properties.getGeminiApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            // Parse the response and print it
            printAiSuggestion(sql, executionTimeMs, responseJson);

        } catch (Exception e) {
            log.error("[shadowDBA] Failed to communicate with Gemini API: {}", e.getMessage());
        }
    }

    private void printAiSuggestion(String sql, long time, String jsonResponse) {
        try {
            // Drill down into the Gemini JSON response to get the actual text
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            String aiText = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Print the final result beautifully to the user's console
            System.out.println("\n===================================================================");
            System.out.println("🚨 [shadowDBA] AI PERFORMANCE ALERT (" + time + "ms)");
            System.out.println("===================================================================");
            System.out.println("Original SQL:\n" + sql + "\n");
            System.out.println("🤖 Gemini DBA Analysis:\n" + aiText.trim());
            System.out.println("===================================================================\n");

        } catch (Exception e) {
            log.error("[shadowDBA] Could not parse Gemini response.", e);
        }
    }
}