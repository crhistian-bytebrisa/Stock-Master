package com.example.stockmaster.logging;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Registra en JSON toda petición HTTP que entra a la API. El log se envía a
 * Loki con el appender de Logback, donde queda indexado por las etiquetas
 * app/host/level y consultable con {app="stock-master"} | json.
 *
 * Campos: timestamp, método, ruta, query, status, duración en ms, user agent,
 * referer y content-length.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("api.request");
    private static final String[] NO_LOG_PATHS = {
            "/actuator/prometheus", "/actuator/health", "/swagger-ui", "/v3/api-docs", "/webjars"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String prefix : NO_LOG_PATHS) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startNanos = System.nanoTime();
        String requestId = resolveRequestId(request);
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;
            log.info(buildJson(request, response, durationMs));
            MDC.remove("requestId");
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String header = request.getHeader("X-Request-Id");
        if (header != null && !header.isBlank()) {
            return header;
        }
        return java.util.UUID.randomUUID().toString();
    }

    private String buildJson(HttpServletRequest request, HttpServletResponse response, long durationMs) {
        StringBuilder json = new StringBuilder(256);
        json.append('{');
        append(json, "timestamp", java.time.Instant.now().toString(), true);
        append(json, "method", request.getMethod(), false);
        append(json, "path", request.getRequestURI(), false);
        append(json, "query", request.getQueryString(), false);
        append(json, "status", String.valueOf(response.getStatus()), false);
        append(json, "durationMs", String.valueOf(durationMs), false);
        append(json, "userAgent", request.getHeader("User-Agent"), false);
        append(json, "referer", request.getHeader("Referer"), false);
        append(json, "contentLength", String.valueOf(request.getContentLengthLong()), false);
        json.append('}');
        return json.toString();
    }

    private void append(StringBuilder json, String key, String value, boolean first) {
        if (!first) {
            json.append(',');
        }
        json.append('"').append(key).append("\":");
        if (value == null) {
            json.append("null");
        } else {
            json.append('"').append(escape(value)).append('"');
        }
    }

    private String escape(String value) {
        StringBuilder out = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}