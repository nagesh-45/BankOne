package com.bankone.role;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fixed catalog of API/UI access codes that can be granted to roles.
 * Authorities in Spring Security are prefixed with {@code ACCESS_}.
 */
public final class AppAccess {

    public static final String DASHBOARD = "DASHBOARD";
    public static final String CUSTOMERS_READ = "CUSTOMERS_READ";
    public static final String CUSTOMERS_WRITE = "CUSTOMERS_WRITE";
    public static final String CUSTOMERS_DELETE = "CUSTOMERS_DELETE";
    public static final String ACCOUNTS_READ = "ACCOUNTS_READ";
    public static final String ACCOUNTS_WRITE = "ACCOUNTS_WRITE";
    public static final String USERS_MANAGE = "USERS_MANAGE";
    public static final String ROLES_MANAGE = "ROLES_MANAGE";
    public static final String POLICIES_MANAGE = "POLICIES_MANAGE";
    /** Bank customer portal: view own accounts only. */
    public static final String PORTAL_ACCOUNTS = "PORTAL_ACCOUNTS";

    public static final String AUTHORITY_PREFIX = "ACCESS_";

    private static final Map<String, String> CATALOG = new LinkedHashMap<>();

    static {
        CATALOG.put(DASHBOARD, "View staff dashboard");
        CATALOG.put(CUSTOMERS_READ, "View customers");
        CATALOG.put(CUSTOMERS_WRITE, "Create and update customers");
        CATALOG.put(CUSTOMERS_DELETE, "Delete customers");
        CATALOG.put(ACCOUNTS_READ, "View accounts and transactions");
        CATALOG.put(ACCOUNTS_WRITE, "Open accounts, deposit, withdraw, transfer, status");
        CATALOG.put(USERS_MANAGE, "Manage employees / staff users");
        CATALOG.put(ROLES_MANAGE, "Create and edit roles and access");
        CATALOG.put(POLICIES_MANAGE, "Manage account opening policies");
        CATALOG.put(PORTAL_ACCOUNTS, "Customer portal — view own accounts only");
    }

    private AppAccess() {
    }

    public static Set<String> allCodes() {
        return CATALOG.keySet();
    }

    public static boolean isKnown(String code) {
        return code != null && CATALOG.containsKey(code.trim().toUpperCase());
    }

    public static String normalize(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    public static String toAuthority(String code) {
        return AUTHORITY_PREFIX + normalize(code);
    }

    public static List<AccessDefinition> catalog() {
        return CATALOG.entrySet().stream()
                .map(e -> new AccessDefinition(e.getKey(), e.getValue()))
                .toList();
    }

    public static Set<String> defaultsForRole(String roleName) {
        return switch (roleName) {
            case "ADMIN" -> CATALOG.keySet().stream()
                    .filter(code -> !PORTAL_ACCOUNTS.equals(code))
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            case "EMPLOYEE" -> Set.of(
                    DASHBOARD, CUSTOMERS_READ, CUSTOMERS_WRITE,
                    ACCOUNTS_READ, ACCOUNTS_WRITE
            );
            case "MANAGER" -> Set.of(
                    DASHBOARD, CUSTOMERS_READ, ACCOUNTS_READ, ACCOUNTS_WRITE, POLICIES_MANAGE
            );
            case "TELLER" -> Set.of(
                    DASHBOARD, CUSTOMERS_READ, ACCOUNTS_READ, ACCOUNTS_WRITE
            );
            case "AUDITOR" -> Set.of(DASHBOARD, CUSTOMERS_READ, ACCOUNTS_READ);
            case "CUSTOMER" -> Set.of(PORTAL_ACCOUNTS);
            default -> Set.of(DASHBOARD);
        };
    }

    public record AccessDefinition(String code, String label) {
    }
}
