package com.bankone.report.dto;

import java.io.Serializable;
import java.util.List;

public record ApprovalsReport(
        String fromDate,
        String toDate,
        List<NamedCount> byStatus,
        List<NamedCount> byStaff,
        long totalRequests
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
