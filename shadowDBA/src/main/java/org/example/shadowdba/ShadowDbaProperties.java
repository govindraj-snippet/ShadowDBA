package org.example.shadowdba;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "shadow-dba")
public class ShadowDbaProperties {

    private boolean enabled = true ;

    private long thresholdMs = 500 ;

    private String geminiApiKey ;


}
