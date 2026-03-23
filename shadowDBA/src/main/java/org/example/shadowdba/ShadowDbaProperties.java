package org.example.shadowdba;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shadow-dba")
public class ShadowDbaProperties {

    private boolean enabled = true;
    private long thresholdMs = 500;
    private String geminiApiKey;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getThresholdMs() { return thresholdMs; }
    public void setThresholdMs(long thresholdMs) { this.thresholdMs = thresholdMs; }

    public String getGeminiApiKey() { return geminiApiKey; }
    public void setGeminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; }
}