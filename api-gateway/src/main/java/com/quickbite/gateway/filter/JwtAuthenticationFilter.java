package com.quickbite.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    @Value("${jwt.secret}")
    private String secret;

    // Public endpoints – no token required
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/register",
        "/api/auth/login",
        "/oauth2/.*",
        "/login/oauth2/.*",
        "/api/restaurants/nearby",
        "/api/restaurants/\\d+",
        "/api/restaurants/cuisine/.*",
        "/api/restaurants/approved",
        "/api/menu/categories/restaurant/\\d+",
        "/api/menu/items/restaurant/\\d+",
        "/api/menu/items/available/\\d+",
        "/api/menu/items/veg/\\d+",
        "/api/menu/items/search",
        "/actuator/.*",
        "/swagger-ui(?:/.*)?",
        "/v3/api-docs(?:/.*)?"
    );

    private SecretKey getSignKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isPublicPath(String path) {
        for (String pattern : PUBLIC_PATHS) {
            if (path.matches(pattern.replace("\\d+", "\\d+").replace(".*", ".*"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // Skip public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Extract token
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Object userIdObj = claims.get("userId");
            String userId = userIdObj != null ? userIdObj.toString() : null;
            String role = claims.get("role", String.class);

            // Forward user identity to downstream services
            ServerHttpRequest.Builder mutatedRequestBuilder = request.mutate();
            if (userId != null) {
                mutatedRequestBuilder.header("X-User-Id", userId);
            }
            if (role != null) {
                mutatedRequestBuilder.header("X-User-Role", role);
            }
            
            System.out.println("reaching api gateway");
            ServerHttpRequest mutatedRequest = mutatedRequestBuilder.build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
