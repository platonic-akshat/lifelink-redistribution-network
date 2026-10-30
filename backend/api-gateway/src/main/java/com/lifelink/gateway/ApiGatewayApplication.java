package com.lifelink.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single HTTP entry point for the surplus-portal React app. Spring Cloud Gateway
 * (reactive/WebFlux) path-routes to backend services and validates Keycloak JWTs
 * via {@link com.lifelink.gateway.config.GatewaySecurityConfig}.
 *
 * Routing is declared in application.yml (spring.cloud.gateway.routes).
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
