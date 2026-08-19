package com.jobpilot.notifications.provider;

import java.util.Map;

/**
 * Pluggable email notification provider interface.
 * Implementations can integrate with SendGrid, AWS SES, Mailgun, SMTP, or logging stubs.
 */
public interface EmailNotificationProvider {

    /**
     * Send an email notification.
     *
     * @param recipientEmail  The recipient's email address.
     * @param recipientName   The recipient's display name.
     * @param subject         Email subject line.
     * @param bodyHtml        Rendered HTML body.
     * @param templateId      Optional provider-level template ID (e.g., SendGrid dynamic template).
     * @param templateData    Key/value template variable substitutions.
     * @return true if the email was accepted for delivery, false otherwise.
     */
    boolean sendEmail(String recipientEmail, String recipientName, String subject,
                      String bodyHtml, String templateId, Map<String, Object> templateData);

    /**
     * Returns the name of this provider implementation.
     */
    String getProviderName();
}
