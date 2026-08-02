package com.bankone.cache;

/**
 * Redis cache region names used with {@code @Cacheable} / {@code @CacheEvict}.
 */
public final class CacheNames {

    public static final String CUSTOMERS = "customers";
    public static final String ACCOUNTS = "accounts";
    public static final String POLICIES = "policies";
    public static final String ROLES = "roles";
    public static final String USERS = "users";
    public static final String TRANSACTIONS = "transactions";
    public static final String BENEFICIARIES = "beneficiaries";
    public static final String DASHBOARD = "dashboard";
    public static final String TRANSFERS = "transfers";
    public static final String REPORTS = "reports";

    private CacheNames() {
    }
}
