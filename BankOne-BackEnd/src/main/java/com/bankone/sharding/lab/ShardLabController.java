package com.bankone.sharding.lab;

import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/lab/shards")
@ConditionalOnProperty(prefix = "app.sharding", name = "enabled", havingValue = "true")
public class ShardLabController {

    public static final String SHARD_HEADER = "X-BankOne-Shard";

    private final ShardLabCustomerService service;

    public ShardLabController(ShardLabCustomerService service) {
        this.service = service;
    }

    @PostMapping("/customers")
    public ResponseEntity<ShardLabCustomerResponse> create(
            @Valid @RequestBody CreateShardLabCustomerRequest request
    ) {
        ShardLabCustomerResponse body = service.create(request);
        return ResponseEntity.ok()
                .header(SHARD_HEADER, body.shard())
                .body(body);
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<ShardLabCustomerResponse> get(@PathVariable UUID id) {
        ShardLabCustomerResponse body = service.getById(id);
        return ResponseEntity.ok()
                .header(SHARD_HEADER, body.shard())
                .body(body);
    }

    @GetMapping("/which/{id}")
    public ResponseEntity<ShardWhichResponse> which(@PathVariable UUID id) {
        ShardWhichResponse body = service.whichShard(id);
        return ResponseEntity.ok()
                .header(SHARD_HEADER, body.shard())
                .body(body);
    }
}
