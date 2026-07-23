package com.bankone.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageRequests {

    private PageRequests() {
    }

    public static Pageable of(
            int page,
            int size,
            String sortBy,
            String sortDir,
            Set<String> allowedFields,
            String defaultField
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String field = allowedFields.contains(sortBy) ? sortBy : defaultField;
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(safePage, safeSize, Sort.by(direction, field));
    }
}
