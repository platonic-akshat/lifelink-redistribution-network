package com.lifelink.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Runs before routing to enforce the JWT check for every request except the
 * starter-grade login endpoint and actuator health/info.
 *
 * This is a starter-grade gate, deliberately not overengineered: it checks
 * for a well-formed, correctly-signed, unexpired bearer token and forwards
 * the {@code role} claim downstream as {@code X-User-Role}. It does not
 * implement per-route role authorization (e.g. "only NGO can approve
 * matches") - that would belong to individual downstream services or a
 * future, more deliberate authorization layer.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String[] PUBLIC_PREFIXES = {
            "/api/auth/",
            "/actuator/"
    };

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public int getOrder() {
        // Run early, well before the routing filter forwards the request.
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing bearer token");
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            return unauthorized(exchange, "Missing bearer token");
        }

        try {
            Claims claims = jwtService.parseAndValidate(token);
            String role = claims.get("role", String.class);

            ServerHttpRequest.Builder mutatedRequest = exchange.getRequest().mutate();
            if (role != null) {
                mutatedRequest.header("X-User-Role", role);
            }
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest.build())
                    .build();
            return chain.filter(mutatedExchange);
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    private boolean isPublic(String path) {
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = response.bufferFactory();
        String body = "{\"error\":\"" + message + "\"}";
        DataBuffer buffer = bufferFactory.wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
