package com.lifelink.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LifelinkRestClientConfig {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate lifelinkRestTemplate() {
        return new RestTemplate();
    }
}
