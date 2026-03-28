package org.example.shadowdba;

public class SqlSanitizer {

    /**
     * Scrubs sensitive literal values from a raw SQL string.
     */
    public static String sanitize(String rawSql) {
        if (rawSql == null || rawSql.isBlank()) {
            return rawSql;
        }

        // 1. Mask String Literals (Anything wrapped in single quotes)
        String sanitized = rawSql.replaceAll("'[^']*'", "'?'");

        // 2. Mask Numeric Literals (Standalone numbers)
        sanitized = sanitized.replaceAll("\\b\\d+\\b", "?");

        return sanitized;
    }
}