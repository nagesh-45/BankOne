package com.bankone.replica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.replica", name = "enabled", havingValue = "true")
public class ReplicaSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReplicaSyncScheduler.class);

    private final ReplicaSyncService syncService;

    public ReplicaSyncScheduler(ReplicaSyncService syncService) {
        this.syncService = syncService;
    }

    @Scheduled(fixedDelayString = "${app.replica.sync-ms:120000}", initialDelayString = "${app.replica.sync-ms:120000}")
    public void scheduledSync() {
        try {
            syncService.syncNow();
        } catch (Exception ex) {
            log.warn("Scheduled replica sync failed: {}", ex.getMessage());
        }
    }
}
