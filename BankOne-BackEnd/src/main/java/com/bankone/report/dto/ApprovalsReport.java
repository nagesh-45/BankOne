package com.bankone.report.dto;

import java.util.List;

public record ApprovalsReport(
        String fromDate,
        String toDate,
        List<NamedCount> byStatus,
        List<NamedCount> byStaff,
        long totalRequests
) {
}
