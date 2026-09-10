package com.lifelink.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single HTTP entry point for both frontends (surplus-portal React app and
 * ngo-dashboard Angular app). Spring Cloud Gateway (reactive/WebFlux) that
 * path-routes to the backend services and enforces a JWT check via
 * {@link com.lifelink.gateway.security.JwtAuthenticationFilter}.
 *
 * Routing itself is declared in application.yml (spring.cloud.gateway.routes)
 * rather than the Java DSL, so no RouteLocator bean is defined here.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
