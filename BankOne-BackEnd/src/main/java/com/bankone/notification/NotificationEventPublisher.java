package com.bankone.notification;

import com.bankone.notification.dto.BankActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Component
public class NotificationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventPublisher.class);

    private final KafkaTemplate<String, BankActionEvent> kafkaTemplate;
    private final boolean enabled;
    private final String topic;

    public NotificationEventPublisher(
            KafkaTemplate<String, BankActionEvent> kafkaTemplate,
            @Value("${app.kafka.enabled:true}") boolean enabled,
            @Value("${app.kafka.notification-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.enabled = enabled;
        this.topic = topic;
    }

    public void publish(
            String action,
            String entityType,
            String entityId,
            String summary,
            String actor,
            String recipientEmail) {

        BankActionEvent event = new BankActionEvent(
                action,
                entityType,
                entityId,
                summary,
                actor == null ? "SYSTEM" : actor,
                Instant.now().toString(),
                StringUtils.hasText(recipientEmail) ? recipientEmail.trim() : null
        );

        if (!enabled) {
            log.info("Kafka disabled — skip notify: {} {}", action, entityId);
            return;
        }

        try {
            // Do not block the HTTP request waiting for broker ack.
            kafkaTemplate.send(topic, entityId, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.warn("Kafka publish failed for {} {}: {}",
                                    action, entityId, ex.getMessage());
                        } else {
                            log.info("Published to {}: {} {} -> {}",
                                    topic, action, entityId, event.getRecipientEmail());
                        }
                    });
        } catch (Exception ex) {
            // Includes TimeoutException when max.block.ms expires (broker down).
            log.warn("Kafka publish skipped for {} {}: {}", action, entityId, ex.getMessage());
        }
    }
}
