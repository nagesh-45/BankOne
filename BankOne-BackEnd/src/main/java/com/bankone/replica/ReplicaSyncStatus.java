package com.bankone.replica;

import java.time.Instant;
import java.util.Map;

public record ReplicaSyncStatus(
        Instant lastSyncAt,
        String message,
        Map<String, Long> rowCounts
) {
}
