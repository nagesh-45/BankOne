package com.bankone.sharding.lab;

import java.time.Instant;
import java.util.UUID;

public record ShardLabCustomerResponse(
        UUID id,
        String fullName,
        String email,
        Instant createdAt,
        String shard
) {
    public static ShardLabCustomerResponse from(ShardLabCustomer entity, String shard) {
        return new ShardLabCustomerResponse(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getCreatedAt(),
                shard
        );
    }
}
