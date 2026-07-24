package com.bankone.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SendGrid Web API (HTTPS :443). Use on Render when SMTP is blocked.
 * Set MAIL_TRANSPORT=sendgrid. API key = MAIL_PASSWORD or SENDGRID_API_KEY.
 */
@Service
@Primary
@ConditionalOnProperty(name = "app.mail.transport", havingValue = "sendgrid")
public class SendGridNotificationMailService implements NotificationMailService {

    private static final Logger log = LoggerFactory.getLogger(SendGridNotificationMailService.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String from;
    private final String fromName;
    private final String fallbackTo;

    public SendGridNotificationMailService(
            @Value("${app.mail.sendgrid-api-key:}") String apiKey,
            @Value("${app.mail.from}") String from,
            @Value("${app.mail.from-name:BankOne}") String fromName,
            @Value("${app.mail.notify-to}") String fallbackTo) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.from = from;
        this.fromName = fromName;
        this.fallbackTo = fallbackTo;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.sendgrid.com")
                .build();
    }

    @Override
    public void send(String toEmail, NotificationEmailContent content) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("SendGrid API key missing (MAIL_PASSWORD / SENDGRID_API_KEY)");
        }
        String to = resolveTo(toEmail);
        log.info("SendGrid HTTP mail from={} to={} subject={}", from, to, content.subject());

        Map<String, Object> fromObj = new LinkedHashMap<>();
        fromObj.put("email", from);
        fromObj.put("name", fromName);

        // text/plain must come before text/html for multipart/alternative semantics
        List<Map<String, String>> contentParts = List.of(
                Map.of("type", "text/plain", "value", content.plainText()),
                Map.of("type", "text/html", "value", content.htmlBody())
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("personalizations", List.of(
                Map.of("to", List.of(Map.of("email", to)))
        ));
        payload.put("from", fromObj);
        payload.put("subject", content.subject());
        payload.put("content", contentParts);
        payload.put("mail_settings", Map.of(
                "sandbox_mode", Map.of("enable", false)
        ));

        restClient.post()
                .uri("/v3/mail/send")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(payload)
                .retrieve()
                .toBodilessEntity();

        log.info("SendGrid email accepted for {}", to);
    }

    private String resolveTo(String toEmail) {
        if (StringUtils.hasText(toEmail)) {
            return toEmail.trim();
        }
        if (StringUtils.hasText(fallbackTo)) {
            return fallbackTo.trim();
        }
        throw new IllegalStateException("No recipient email (customer email missing and MAIL_NOTIFY_TO unset)");
    }
}
