package com.bankone.notification;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Always registered (Mailpit / SMTP). When SendGrid is enabled it is {@code @Primary}.
 */
@Service
public class SmtpNotificationMailService implements NotificationMailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpNotificationMailService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String fallbackTo;

    public SmtpNotificationMailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from,
            @Value("${app.mail.notify-to}") String fallbackTo) {
        this.mailSender = mailSender;
        this.from = from;
        this.fallbackTo = fallbackTo;
    }

    @Override
    public void send(String toEmail, String subject, String body) {
        String to = resolveTo(toEmail);
        log.info("SMTP mail from={} to={}", from, to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
            log.info("SMTP email sent to {}", to);
        } catch (Exception ex) {
            throw new IllegalStateException("SMTP send failed: " + ex.getMessage(), ex);
        }
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
