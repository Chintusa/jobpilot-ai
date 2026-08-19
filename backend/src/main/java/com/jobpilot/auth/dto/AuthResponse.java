package com.jobpilot.auth.dto;

import com.jobpilot.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private UserDto user;
}
