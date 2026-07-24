package com.bankone.notification;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Default mail path (Mailpit locally). Used whenever SendGrid HTTP bean is not active.
 */
@Service
@ConditionalOnMissingBean(NotificationMailService.class)
public class SmtpNotificationMailService implements NotificationMailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpNotificationMailService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String notifyTo;

    public SmtpNotificationMailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from,
            @Value("${app.mail.notify-to}") String notifyTo) {
        this.mailSender = mailSender;
        this.from = from;
        this.notifyTo = notifyTo;
    }

    @Override
    public void send(String subject, String body) {
        log.info("SMTP mail from={} to={}", from, notifyTo);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(notifyTo);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
            log.info("SMTP email sent to {}", notifyTo);
        } catch (Exception ex) {
            throw new IllegalStateException("SMTP send failed: " + ex.getMessage(), ex);
        }
    }
}
