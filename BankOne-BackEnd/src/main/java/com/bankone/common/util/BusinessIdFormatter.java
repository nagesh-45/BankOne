package com.bankone.common.util;

public final class BusinessIdFormatter {

    private static final int MIN_DIGITS = 5;

    private BusinessIdFormatter() {
    }

    public static String customerCode(Long customerId) {
        return format("C", customerId);
    }

    public static String employeeCode(Long userId) {
        return format("E", userId);
    }

    public static Long parseCustomerId(String value) {
        return parse("C", value);
    }

    public static Long parseEmployeeId(String value) {
        return parse("E", value);
    }

    private static String format(String prefix, Long id) {
        if (id == null || id <= 0) {
            return "";
        }

        // Keep a 5-digit floor (C00001). Past 99999, width expands (C100000).
        String digits = Long.toString(id);
        if (digits.length() < MIN_DIGITS) {
            digits = String.format("%0" + MIN_DIGITS + "d", id);
        }
        return prefix + digits;
    }

    private static Long parse(String prefix, String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        String upper = trimmed.toUpperCase();
        if (upper.startsWith(prefix) && upper.length() > 1) {
            try {
                return Long.parseLong(upper.substring(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }
}
