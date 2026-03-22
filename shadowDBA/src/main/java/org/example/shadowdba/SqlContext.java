package org.example.shadowdba;

public class SqlContext {
    // ThreadLocal ensures that each web request has its own isolated memory space
    private static final ThreadLocal<String> currentSql = new ThreadLocal<>();

    public static void setSql(String sql) {
        currentSql.set(sql);
    }

    public static String getSql() {
        return currentSql.get();
    }

    public static void clear() {
        currentSql.remove();
    }
}