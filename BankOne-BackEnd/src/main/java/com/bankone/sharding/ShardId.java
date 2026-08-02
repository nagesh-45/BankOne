package com.bankone.sharding;

/**
 * Physical shard identifiers for the local lab (2 databases).
 */
public enum ShardId {
    S0("s0"),
    S1("s1");

    private final String code;

    ShardId(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static ShardId fromIndex(int index) {
        return switch (index) {
            case 0 -> S0;
            case 1 -> S1;
            default -> throw new IllegalArgumentException("Unknown shard index: " + index);
        };
    }
}
