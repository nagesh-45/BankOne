package com.bankone.sharding;

/**
 * Holds the active shard for the current thread so {@link org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource}
 * can pick s0 vs s1.
 */
public final class ShardContext {

    private static final ThreadLocal<ShardId> CURRENT = new ThreadLocal<>();

    private ShardContext() {
    }

    public static void set(ShardId shardId) {
        CURRENT.set(shardId);
    }

    public static ShardId get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
