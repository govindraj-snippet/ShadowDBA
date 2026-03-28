package org.example.shadowdba;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeminiOptimizerService {

    private static final Logger log = LoggerFactory.getLogger(GeminiOptimizerService.class);

    private final RestClient restClient;
    private final ShadowDbaProperties properties;
    private final ObjectMapper objectMapper;

    private final Set<Integer> analyzedQueries = ConcurrentHashMap.newKeySet();
    private static final String REPORT_FILE = "shadowdba-reports.md";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    // ✨ THE ULTIMATE PROMPT: Forces a strict, predictable Markdown template every time
  //  private static final String PROMPT_TEMPLATE =
    // ✨ THE NON-INVASIVE ARCHITECT PROMPT
    private static final String PROMPT_TEMPLATE =
            "You are an expert Spring Boot 3 Architect. The following Hibernate SQL query took %d ms to execute. " +
                    "CRITICAL RULES: \n" +
                    "1. DO NOT provide raw SQL scripts. \n" +
                    "2. Use ONLY modern jakarta.persistence.* annotations. \n" +
                    "3. STRICTLY NON-INVASIVE: Do NOT add new fields, new columns, or lifecycle hooks (like @PrePersist) to the Entity. \n" +
                    "4. Optimize using ONLY the existing fields. Use native Spring Data JPA keywords (like IgnoreCase) or optimized @Query rewrites. \n" +
                    "5. The @Index annotation MUST ONLY be placed inside @Table(indexes = {...}) at the class level. \n" +
                    "6. Provide a SURGICAL, MINIMAL fix. Output only the exact lines to add/change.\n\n" +
                    "Format your response EXACTLY using the Markdown structure below:\n\n" +
                    "### 💡 Root Cause\n" +
                    "(Explain the exact bottleneck in under 40 words)\n\n" +
                    "### 🎯 The Minimal Fix\n" +
                    "**Target File:** (e.g., Customer.java, CustomerRepository.java)\n\n" +
                    "```text\n" +
                    "(Provide ONLY the specific Java/Spring code. No boilerplate.)\n" +
                    "```\n\n" +
                    "Query: %s";

    public GeminiOptimizerService(ShadowDbaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    @Async
    public void analyzeQuery(String rawSql, long executionTimeMs) {
        if (properties.getGeminiApiKey() == null || properties.getGeminiApiKey().isBlank()) {
            log.warn("[shadowDBA] Gemini API Key is missing.");
            return;
        }

        // Scrub sensitive data
        String safeSql = SqlSanitizer.sanitize(rawSql);

        // Memory bank check
        int queryHash = safeSql.hashCode();
        if (!analyzedQueries.add(queryHash)) {
            return;
        }

        try {
            String prompt = String.format(PROMPT_TEMPLATE, executionTimeMs, safeSql);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
            );

            String responseJson = restClient.post()
                    .uri(GEMINI_URL + "?key=" + properties.getGeminiApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            saveAndPrintAiSuggestion(safeSql, executionTimeMs, responseJson);

        } catch (Exception e) {
            log.error("[shadowDBA] Failed to communicate with Gemini API: {}", e.getMessage());
            analyzedQueries.remove(queryHash);
        }
    }

    private void saveAndPrintAiSuggestion(String safeSql, long time, String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            String aiText = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            System.out.println("\n🚨 [shadowDBA] AI analyzed a slow query (" + time + "ms). Saved to " + REPORT_FILE);

            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String markdownReport = String.format(
                    "\n## 🚨 Slow Query Alert: %sms\n**Time:** %s\n\n### 🛡️ Sanitized SQL:\n```sql\n%s\n```\n\n%s\n\n---\n",
                    time, timeStamp, safeSql, aiText.trim()
            );

            Path path = Paths.get(REPORT_FILE);
            if (!Files.exists(path)) {
                Files.writeString(path, "# shadowDBA Performance Reports\n\n");
            }
            Files.writeString(path, markdownReport, StandardOpenOption.APPEND);

        } catch (Exception e) {
            log.error("[shadowDBA] Could not parse Gemini response or write to file.", e);
        }
    }
}