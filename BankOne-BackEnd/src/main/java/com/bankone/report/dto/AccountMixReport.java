package com.bankone.report.dto;

import java.io.Serializable;
import java.util.List;

public record AccountMixReport(
        List<NamedCount> byType,
        List<NamedCount> byStatus,
        long totalAccounts
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
