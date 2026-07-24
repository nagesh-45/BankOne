package com.bankone.notification;

import com.bankone.notification.dto.BankActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationMailService mailService;

    public NotificationEventConsumer(NotificationMailService mailService) {
        this.mailService = mailService;
    }

    @KafkaListener(topics = "${app.kafka.notification-topic}", groupId = "bankone-notification")
    public void onMessage(BankActionEvent event) {
        log.info("Consumed: {} {} — {}", event.getAction(), event.getEntityId(), event.getSummary());
        try {
            String subject = "[BankOne] " + event.getAction() + " #" + event.getEntityId();
            String body = "Action: " + event.getAction() + "\n"
                    + "Type: " + event.getEntityType() + "\n"
                    + "Id: " + event.getEntityId() + "\n"
                    + "Actor: " + event.getActor() + "\n"
                    + "When: " + event.getOccurredAt() + "\n\n"
                    + event.getSummary() + "\n";
            mailService.send(subject, body);
            log.info("Email sent for {}", event.getAction());
        } catch (Exception ex) {
            log.warn("Email failed for {}: {}", event.getAction(), ex.toString(), ex);
        }
    }
}
