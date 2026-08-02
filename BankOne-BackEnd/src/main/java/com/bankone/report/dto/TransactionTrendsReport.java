package com.bankone.report.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record TransactionTrendsReport(
        String fromDate,
        String toDate,
        List<DailyTrendPoint> daily,
        BigDecimal totalCreditAmount,
        BigDecimal totalDebitAmount,
        long totalCreditCount,
        long totalDebitCount
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
