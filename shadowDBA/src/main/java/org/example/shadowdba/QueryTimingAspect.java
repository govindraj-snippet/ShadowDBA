package org.example.shadowdba;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class QueryTimingAspect {

    private static final Logger log = LoggerFactory.getLogger(QueryTimingAspect.class);

    private final ShadowDbaProperties properties;
    private final GeminiOptimizerService aiService;

    public QueryTimingAspect(ShadowDbaProperties properties, GeminiOptimizerService aiService) {
        this.properties = properties;
        this.aiService = aiService;
    }

    @Around("execution(* org.springframework.data.repository.Repository+.*(..))")
    public Object timeDatabaseQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            String rawSql = SqlContext.getSql();

            if (executionTime > properties.getThresholdMs() && rawSql != null) {
                log.warn("[shadowDBA] Intercepted slow query ({}ms). Sending to AI for analysis...", executionTime);
                aiService.analyzeQuery(rawSql, executionTime);
            }

            SqlContext.clear();
        }
    }
}