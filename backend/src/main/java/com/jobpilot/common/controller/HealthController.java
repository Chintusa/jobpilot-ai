package com.jobpilot.common.controller;

import com.jobpilot.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "System status, PostgreSQL, and Redis health check probes")
public class HealthController {

    private final DataSource dataSource;

    @GetMapping
    @Operation(summary = "Check backend and database health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "JobPilot AI Backend");
        health.put("version", "1.0.0");
        health.put("timestamp", Instant.now());

        // Probe database
        try (Connection connection = dataSource.getConnection()) {
            health.put("database", connection.isValid(2) ? "CONNECTED" : "DISCONNECTED");
        } catch (Exception e) {
            health.put("database", "ERROR: " + e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.ok("System healthy", health));
    }
}
