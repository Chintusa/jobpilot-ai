package com.jobpilot.notifications.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Mock email notification provider that logs email sends for development/testing.
 * Records all sent emails in-memory for test assertion.
 */
@Slf4j
@Component
public class MockEmailNotificationProvider implements EmailNotificationProvider {

    /**
     * Record of a sent email for test verification.
     */
    public record SentEmail(String recipientEmail, String recipientName, String subject,
                            String bodyHtml, String templateId, Map<String, Object> templateData) {}

    private final CopyOnWriteArrayList<SentEmail> sentEmails = new CopyOnWriteArrayList<>();

    @Override
    public boolean sendEmail(String recipientEmail, String recipientName, String subject,
                             String bodyHtml, String templateId, Map<String, Object> templateData) {
        SentEmail record = new SentEmail(recipientEmail, recipientName, subject, bodyHtml, templateId, templateData);
        sentEmails.add(record);
        log.info("[MOCK EMAIL] To: {} <{}>, Subject: '{}', Template: {}", recipientName, recipientEmail, subject, templateId);
        return true;
    }

    @Override
    public String getProviderName() {
        return "MOCK_EMAIL_PROVIDER";
    }

    /**
     * Returns all emails sent via this mock provider (for testing purposes).
     */
    public CopyOnWriteArrayList<SentEmail> getSentEmails() {
        return sentEmails;
    }

    /**
     * Clear all recorded emails.
     */
    public void clearSentEmails() {
        sentEmails.clear();
    }
}
