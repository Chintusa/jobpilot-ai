package com.jobpilot.automation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveInterventionRequest {

    private String resolutionPayload; // Answer, OTP code, confirmation token
    private String action; // RESOLVE, CANCEL
}
