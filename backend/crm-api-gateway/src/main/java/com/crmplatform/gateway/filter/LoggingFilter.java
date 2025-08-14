package com.crmplatform.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();
        String timestamp = LocalDateTime.now().format(formatter);
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String remoteAddress = request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        log.info("=== REQUEST START ===");
        log.info("Timestamp: {}", timestamp);
        log.info("Method: {} {}", method, path);
        log.info("Remote Address: {}", remoteAddress);
        log.info("User-Agent: {}", userAgent);
        log.info("Headers: {}", request.getHeaders());

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null ? 
                            exchange.getResponse().getStatusCode().value() : 0;

                    log.info("=== REQUEST END ===");
                    log.info("Status: {}", statusCode);
                    log.info("Duration: {}ms", duration);
                    log.info("Timestamp: {}", LocalDateTime.now().format(formatter));
                    log.info("==================");
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // Log after all other filters
    }
} 