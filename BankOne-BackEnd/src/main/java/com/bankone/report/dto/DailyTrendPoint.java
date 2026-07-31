package com.bankone.report.dto;

import java.math.BigDecimal;

public record DailyTrendPoint(
        String date,
        BigDecimal creditAmount,
        BigDecimal debitAmount,
        long creditCount,
        long debitCount
) {
}
