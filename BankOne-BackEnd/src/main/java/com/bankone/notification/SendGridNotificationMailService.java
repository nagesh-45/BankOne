package com.bankone.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * SendGrid Web API (HTTPS :443). Use on Render when SMTP is blocked.
 * API key = MAIL_PASSWORD (or SENDGRID_API_KEY).
 */
@Service
@ConditionalOnProperty(name = "app.mail.transport", havingValue = "sendgrid", matchIfMissing = false)
public class SendGridNotificationMailService implements NotificationMailService {

    private static final Logger log = LoggerFactory.getLogger(SendGridNotificationMailService.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String from;
    private final String notifyTo;

    public SendGridNotificationMailService(
            @Value("${app.mail.sendgrid-api-key:}") String apiKey,
            @Value("${app.mail.from}") String from,
            @Value("${app.mail.notify-to}") String notifyTo) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.from = from;
        this.notifyTo = notifyTo;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.sendgrid.com")
                .build();
    }

    @Override
    public void send(String subject, String body) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("SendGrid API key missing (MAIL_PASSWORD / SENDGRID_API_KEY)");
        }
        log.info("SendGrid HTTP mail from={} to={}", from, notifyTo);

        Map<String, Object> payload = Map.of(
                "personalizations", List.of(
                        Map.of("to", List.of(Map.of("email", notifyTo)))
                ),
                "from", Map.of("email", from),
                "subject", subject,
                "content", List.of(
                        Map.of("type", "text/plain", "value", body)
                )
        );

        restClient.post()
                .uri("/v3/mail/send")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(payload)
                .retrieve()
                .toBodilessEntity();

        log.info("SendGrid email accepted for {}", notifyTo);
    }
}
