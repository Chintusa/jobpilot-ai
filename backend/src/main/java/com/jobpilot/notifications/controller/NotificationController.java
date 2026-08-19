package com.jobpilot.notifications.controller;

import com.jobpilot.common.dto.ApiResponse;
import com.jobpilot.notifications.dto.NotificationDto;
import com.jobpilot.notifications.dto.NotificationPreferencesDto;
import com.jobpilot.notifications.dto.UpdateNotificationPreferencesRequest;
import com.jobpilot.notifications.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications, email alerts, preference management, and event-driven lifecycle alerts")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get candidate notifications (optionally filtered by category)")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "category", required = false) String category) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        List<NotificationDto> notifications = notificationService.getUserNotifications(email, category);
        return ResponseEntity.ok(ApiResponse.ok(notifications));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification badge count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        long count = notificationService.getUnreadCount(email);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("unreadCount", count)));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark specific notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable("id") UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.ok("Marked as read", null));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all candidate notifications as read")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        int count = notificationService.markAllAsRead(email);
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", Map.of("markedCount", count)));
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get candidate notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreferencesDto>> getPreferences(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        NotificationPreferencesDto prefs = notificationService.getPreferences(email);
        return ResponseEntity.ok(ApiResponse.ok(prefs));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update candidate notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreferencesDto>> updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateNotificationPreferencesRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        NotificationPreferencesDto prefs = notificationService.updatePreferences(email, request);
        return ResponseEntity.ok(ApiResponse.ok("Notification preferences updated", prefs));
    }
}
