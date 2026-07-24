package com.bankone.notification;

import com.bankone.notification.dto.BankActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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
            String actor) {

        BankActionEvent event = new BankActionEvent(
                action,
                entityType,
                entityId,
                summary,
                actor == null ? "SYSTEM" : actor,
                Instant.now().toString()
        );

        if (!enabled) {
            log.info("Kafka disabled — skip notify: {} {}", action, entityId);
            return;
        }

        try {
            kafkaTemplate.send(topic, entityId, event);
            log.info("Published to {}: {} {}", topic, action, entityId);
        } catch (Exception ex) {
            // Fail-soft: business save already succeeded; don't break the API
            log.warn("Kafka publish failed for {} {}: {}", action, entityId, ex.getMessage());
        }
    }
}