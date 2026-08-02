package com.bankone.sharding;

import java.util.UUID;

/**
 * Maps a routing key (lab UUID) to a shard: {@code floorMod(hash, shardCount)}.
 */
public class ShardRouter {

    private final int shardCount;

    public ShardRouter(ShardingProperties properties) {
        this.shardCount = properties.getShardCount();
        if (shardCount < 1) {
            throw new IllegalStateException("app.sharding.shard-count must be >= 1");
        }
    }

    public ShardId forKey(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Routing key UUID is required");
        }
        int index = Math.floorMod(id.hashCode(), shardCount);
        return ShardId.fromIndex(index);
    }
}
