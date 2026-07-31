package com.bankone.report.dto;

import java.util.List;

public record AccountMixReport(
        List<NamedCount> byType,
        List<NamedCount> byStatus,
        long totalAccounts
) {
}
