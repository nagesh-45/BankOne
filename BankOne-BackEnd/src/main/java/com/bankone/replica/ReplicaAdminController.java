package com.bankone.replica;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/replica")
@ConditionalOnProperty(prefix = "app.replica", name = "enabled", havingValue = "true")
public class ReplicaAdminController {

    private final ReplicaSyncService syncService;

    public ReplicaAdminController(ReplicaSyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/sync")
    public ResponseEntity<ReplicaSyncStatus> syncNow() {
        return ResponseEntity.ok(syncService.syncNow());
    }

    @GetMapping("/status")
    public ResponseEntity<ReplicaSyncStatus> status() {
        return ResponseEntity.ok(syncService.status());
    }
}
