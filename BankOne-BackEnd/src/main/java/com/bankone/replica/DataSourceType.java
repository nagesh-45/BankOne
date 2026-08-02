package com.bankone.replica;

/**
 * Lookup keys for {@link org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource}.
 */
public enum DataSourceType {
    WRITE,
    READ
}
