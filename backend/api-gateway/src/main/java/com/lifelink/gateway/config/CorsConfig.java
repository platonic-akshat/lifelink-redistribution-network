package com.lifelink.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * A {@link CorsWebFilter} runs as a plain WebFilter, upstream of both Spring
 * Cloud Gateway's own routing (which has its own, separate
 * {@code spring.cloud.gateway.globalcors} mechanism) and the normal WebFlux
 * dispatcher used by locally-hosted {@code @RestController}s like
 * {@link com.lifelink.gateway.web.AuthController}. Using one filter here
 * covers both uniformly instead of configuring CORS twice.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Dev-only allowlist for the two local frontend dev servers. A real
        // deployment should read this from config per-environment.
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(source);
    }
}
