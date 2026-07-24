package com.bankone.notification;

import com.bankone.notification.dto.BankActionEvent;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String notifyTo;

    public NotificationEventConsumer(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from,
            @Value("${app.mail.notify-to}") String notifyTo) {
        this.mailSender = mailSender;
        this.from = from;
        this.notifyTo = notifyTo;
    }

    @KafkaListener(topics = "${app.kafka.notification-topic}", groupId = "bankone-notification")
    public void onMessage(BankActionEvent event) {
        log.info("Consumed: {} {} — {}", event.getAction(), event.getEntityId(), event.getSummary());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(notifyTo);
            helper.setSubject("[BankOne] " + event.getAction() + " #" + event.getEntityId());
            helper.setText(
                    "Action: " + event.getAction() + "\n"
                            + "Type: " + event.getEntityType() + "\n"
                            + "Id: " + event.getEntityId() + "\n"
                            + "Actor: " + event.getActor() + "\n"
                            + "When: " + event.getOccurredAt() + "\n\n"
                            + event.getSummary() + "\n",
                    false
            );
            mailSender.send(message);
            log.info("Email sent to {} for {}", notifyTo, event.getAction());
        } catch (Exception ex) {
            log.warn("Email failed for {}: {}", event.getAction(), ex.getMessage());
        }
    }
}