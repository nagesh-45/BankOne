package com.bankone.report.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record DailyTrendPoint(
        String date,
        BigDecimal creditAmount,
        BigDecimal debitAmount,
        long creditCount,
        long debitCount
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
