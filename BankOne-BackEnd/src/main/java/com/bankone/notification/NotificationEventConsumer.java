package com.bankone.notification;

import com.bankone.notification.dto.BankActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationMailService mailService;
    private final NotificationEmailComposer emailComposer;

    public NotificationEventConsumer(
            NotificationMailService mailService,
            NotificationEmailComposer emailComposer) {
        this.mailService = mailService;
        this.emailComposer = emailComposer;
    }

    @KafkaListener(topics = "${app.kafka.notification-topic}", groupId = "bankone-notification")
    public void onMessage(BankActionEvent event) {
        log.info("Consumed: {} {} — {} (to={})",
                event.getAction(),
                event.getEntityId(),
                event.getSummary(),
                event.getRecipientEmail());
        try {
            if (!StringUtils.hasText(event.getRecipientEmail())) {
                log.warn("No customer email on event {} {}; falling back to MAIL_NOTIFY_TO if set",
                        event.getAction(), event.getEntityId());
            }
            NotificationEmailContent content = emailComposer.compose(event);
            mailService.send(event.getRecipientEmail(), content);
            log.info("Email sent for {} to {} subject={}",
                    event.getAction(), event.getRecipientEmail(), content.subject());
        } catch (Exception ex) {
            log.warn("Email failed for {}: {}", event.getAction(), ex.toString(), ex);
        }
    }
}
