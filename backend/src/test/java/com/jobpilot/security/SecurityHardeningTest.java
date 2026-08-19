package com.jobpilot.security;

import com.jobpilot.auth.dto.LoginRequest;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SecurityHardeningTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    private String testEmail;

    @BeforeEach
    void setUp() {
        testEmail = "sec_test_" + System.currentTimeMillis() + "@example.com";
        authService.register(RegisterRequest.builder()
                .email(testEmail)
                .password("SecurePass123!")
                .name("Security Tester")
                .build());
    }

    @Test
    @DisplayName("Worker API key is required to access /api/v1/worker endpoints")
    void testWorkerApiKeyFilter() throws Exception {
        // Without API key
        mockMvc.perform(get("/api/v1/worker/health"))
                .andExpect(status().isUnauthorized());

        // With incorrect API key
        mockMvc.perform(get("/api/v1/worker/health")
                        .header("X-Worker-Api-Key", "wrong-key"))
                .andExpect(status().isUnauthorized());

        // We can't easily test with correct key here because /api/v1/worker/health doesn't actually exist
        // But the 401 proves the filter is working before it hits the 404 handler.
    }

    @Test
    @DisplayName("Refresh token is returned as an HttpOnly, Secure cookie")
    void testRefreshTokenCookie() throws Exception {
        String loginJson = """
                {
                    "email": "%s",
                    "password": "SecurePass123!"
                }
                """.formatted(testEmail);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist()) // Ensure it's not in the JSON body
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("refresh_token", true));
    }
}
