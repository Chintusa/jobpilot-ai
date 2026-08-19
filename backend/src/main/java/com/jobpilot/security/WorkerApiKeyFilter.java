package com.jobpilot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class WorkerApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.worker.api-key:default-worker-secret-key-change-in-prod}")
    private String expectedApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        if (path.startsWith("/api/v1/worker") || path.startsWith("/api/worker")) {
            String providedKey = request.getHeader("X-Worker-Api-Key");
            if (providedKey == null || !providedKey.equals(expectedApiKey)) {
                log.warn("Unauthorized worker access attempt from IP: {}", request.getRemoteAddr());
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Unauthorized: Invalid or missing API Key");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
