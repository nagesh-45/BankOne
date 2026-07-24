package com.bankone.notification;

/**
 * Sends notification emails. Local: SMTP (Mailpit). Render: SendGrid HTTP API
 * because outbound SMTP ports are often blocked.
 */
public interface NotificationMailService {

    void send(String toEmail, String subject, String body);
}
