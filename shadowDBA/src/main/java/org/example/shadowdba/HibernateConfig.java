package org.example.shadowdba;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

    @Bean
    public HibernatePropertiesCustomizer hibernateCustomizer(AiSqlInspector inspector) {
        // This injects our inspector directly into the user's Hibernate session
        return (properties) -> properties.put(
                "hibernate.session_factory.statement_inspector", inspector
        );
    }
}