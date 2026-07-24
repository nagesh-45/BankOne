package com.bankone.notification;

/**
 * Multipart email: plain text + HTML (better deliverability than HTML-only).
 */
public record NotificationEmailContent(
        String subject,
        String plainText,
        String htmlBody
) {
}
