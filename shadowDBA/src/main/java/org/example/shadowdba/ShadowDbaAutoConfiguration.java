package org.example.shadowdba;

//package org.example.shadowdba;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@AutoConfiguration
@EnableAsync // Allows us to talk to Gemini without slowing down the user's app
@EnableConfigurationProperties(ShadowDbaProperties.class)
@ConditionalOnProperty(prefix = "shadow-dba", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "org.example.shadowdba")
public class ShadowDbaAutoConfiguration {
    // Spring Boot will automatically scan and load our interceptors and services
}