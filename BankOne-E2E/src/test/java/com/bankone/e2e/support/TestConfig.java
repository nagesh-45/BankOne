package com.bankone.e2e.support;

public final class TestConfig {

    private TestConfig() {
    }

    public static String baseUrl() {
        return env("BANKONE_BASE_URL", "http://localhost:4200");
    }

    public static String username() {
        return env("BANKONE_USERNAME", "admin");
    }

    public static String password() {
        return env("BANKONE_PASSWORD", "Admin@123");
    }

    /**
     * Headed locally by default; headless in CI ({@code CI}/{@code GITHUB_ACTIONS}).
     * Override anytime with {@code BANKONE_E2E_HEADLESS=true|false}.
     */
    public static boolean headless() {
        String override = System.getenv("BANKONE_E2E_HEADLESS");
        if (override != null && !override.isBlank()) {
            return Boolean.parseBoolean(override.trim());
        }
        return isCi();
    }

    private static boolean isCi() {
        return truthy(System.getenv("CI")) || truthy(System.getenv("GITHUB_ACTIONS"));
    }

    private static boolean truthy(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String v = value.trim();
        return "true".equalsIgnoreCase(v) || "1".equals(v) || "yes".equalsIgnoreCase(v);
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
