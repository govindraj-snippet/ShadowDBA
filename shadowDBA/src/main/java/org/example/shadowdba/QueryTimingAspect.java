package org.example.shadowdba;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class QueryTimingAspect {

    private final ShadowDbaProperties properties;

    public QueryTimingAspect(ShadowDbaProperties properties) {
        this.properties = properties;
    }

    // This annotation tells Spring to wrap EVERY method inside a Spring Data Repository
    @Around("execution(* org.springframework.data.repository.Repository+.*(..))")
    public Object timeDatabaseQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        // If the user disabled shadowDBA in properties, just run the query normally
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();

        try {
            // This executes the actual database query
            return joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            String rawSql = SqlContext.getSql();

            // If it's slower than the user's threshold, trigger the alert!
            if (executionTime > properties.getThresholdMs() && rawSql != null) {
                log.warn("\n🔥 [shadowDBA] SLOW QUERY CAUGHT: {}ms", executionTime);
                log.warn("SQL: {}", rawSql);

                // For Day 3: This is exactly where we will call our Gemini AI Service!
            }

            // Always clean up the memory to prevent memory leaks
            SqlContext.clear();
        }
    }
}