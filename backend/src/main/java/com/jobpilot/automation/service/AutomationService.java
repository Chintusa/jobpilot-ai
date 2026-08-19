package com.jobpilot.automation.service;

import com.jobpilot.applications.entity.Application;
import com.jobpilot.applications.entity.ApplicationEvent;
import com.jobpilot.applications.repository.ApplicationRepository;
import com.jobpilot.automation.dto.AgentStatusDto;
import com.jobpilot.automation.dto.HumanInterventionDto;
import com.jobpilot.automation.dto.ResolveInterventionRequest;
import com.jobpilot.automation.entity.AgentSchedule;
import com.jobpilot.automation.entity.HumanIntervention;
import com.jobpilot.automation.repository.AgentScheduleRepository;
import com.jobpilot.automation.repository.HumanInterventionRepository;
import com.jobpilot.common.exception.ResourceNotFoundException;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutomationService {

    private final HumanInterventionRepository interventionRepository;
    private final AgentScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public List<HumanInterventionDto> getUserInterventions(String userEmail, String status) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        List<HumanIntervention> list = (status != null && !status.equalsIgnoreCase("ALL"))
                ? interventionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), status.toUpperCase())
                : interventionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return list.stream().map(HumanInterventionDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HumanInterventionDto getInterventionById(String userEmail, UUID interventionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        HumanIntervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new ResourceNotFoundException("Intervention not found: " + interventionId));

        return HumanInterventionDto.fromEntity(intervention);
    }

    @Transactional
    public HumanInterventionDto createIntervention(String userEmail, UUID applicationId, String reason, String description, String context) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Application application = applicationId != null ? applicationRepository.findById(applicationId).orElse(null) : null;

        HumanIntervention intervention = HumanIntervention.builder()
                .user(user)
                .application(application)
                .reason(reason)
                .type(reason)
                .description(description)
                .status("PENDING")
                .requiredInput("CAPTCHA".equalsIgnoreCase(reason) || "MFA".equalsIgnoreCase(reason) ? "SECURITY_VERIFICATION" : "TEXT")
                .requiredInputType("CAPTCHA".equalsIgnoreCase(reason) || "MFA".equalsIgnoreCase(reason) ? "SECURITY_VERIFICATION" : "TEXT")
                .context(context != null ? context : "{}")
                .build();

        if (application != null) {
            application.setStatus("PREPARING");
            application.setPreparationState("REQUIRES_USER_INPUT");
            application.getEvents().add(ApplicationEvent.builder()
                    .application(application)
                    .eventType("HUMAN_INTERVENTION_TRIGGERED")
                    .message("Application paused: " + reason + " - " + description)
                    .build());
            applicationRepository.save(application);
        }

        HumanIntervention saved = interventionRepository.save(intervention);
        log.info("Created HumanIntervention id={}, reason={}, user={}", saved.getId(), reason, userEmail);
        return HumanInterventionDto.fromEntity(saved);
    }

    @Transactional
    public HumanInterventionDto resolveIntervention(String userEmail, UUID interventionId, ResolveInterventionRequest request) {
        HumanIntervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new ResourceNotFoundException("Intervention not found: " + interventionId));

        intervention.setStatus("RESOLVED");
        if (request != null && request.getResolutionPayload() != null) {
            intervention.setResolutionPayload(request.getResolutionPayload());
        }
        intervention.setResolvedAt(Instant.now());

        if (intervention.getApplication() != null) {
            Application app = intervention.getApplication();
            app.setPreparationState("USER_APPROVED");
            app.getEvents().add(ApplicationEvent.builder()
                    .application(app)
                    .eventType("INTERVENTION_RESOLVED")
                    .message("Candidate completed required human intervention: " + intervention.getReason())
                    .build());
            applicationRepository.save(app);
        }

        HumanIntervention saved = interventionRepository.save(intervention);
        log.info("Intervention {} resolved by candidate {}", interventionId, userEmail);
        return HumanInterventionDto.fromEntity(saved);
    }

    @Transactional
    public HumanInterventionDto cancelIntervention(String userEmail, UUID interventionId) {
        HumanIntervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new ResourceNotFoundException("Intervention not found: " + interventionId));

        intervention.setStatus("CANCELLED");
        intervention.setResolvedAt(Instant.now());

        if (intervention.getApplication() != null) {
            Application app = intervention.getApplication();
            app.setStatus("MATCHED");
            app.getEvents().add(ApplicationEvent.builder()
                    .application(app)
                    .eventType("INTERVENTION_CANCELLED")
                    .message("Candidate cancelled intervention flow")
                    .build());
            applicationRepository.save(app);
        }

        HumanIntervention saved = interventionRepository.save(intervention);
        log.info("Intervention {} cancelled by candidate {}", interventionId, userEmail);
        return HumanInterventionDto.fromEntity(saved);
    }

    @Transactional
    public AgentStatusDto getAgentStatus(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        AgentSchedule schedule = scheduleRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    AgentSchedule s = AgentSchedule.builder()
                            .user(user)
                            .status("ACTIVE")
                            .enabled(true)
                            .cronExpression("0 0/30 * * * ?")
                            .lastRunAt(Instant.now().minusSeconds(1800))
                            .nextRunAt(Instant.now().plusSeconds(1800))
                            .build();
                    return scheduleRepository.save(s);
                });

        return AgentStatusDto.fromEntity(schedule);
    }

    @Transactional
    public AgentStatusDto toggleAgent(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        AgentSchedule schedule = scheduleRepository.findByUserId(user.getId())
                .orElseGet(() -> AgentSchedule.builder()
                        .user(user)
                        .status("ACTIVE")
                        .enabled(true)
                        .build());

        if ("ACTIVE".equalsIgnoreCase(schedule.getStatus())) {
            schedule.setStatus("PAUSED");
            schedule.setEnabled(false);
        } else {
            schedule.setStatus("ACTIVE");
            schedule.setEnabled(true);
        }

        AgentSchedule saved = scheduleRepository.save(schedule);
        return AgentStatusDto.fromEntity(saved);
    }
}
