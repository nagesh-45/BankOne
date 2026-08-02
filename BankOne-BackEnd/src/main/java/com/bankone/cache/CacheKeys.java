package com.bankone.cache;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Builds stable Redis cache key fragments for search/page queries.
 */
public final class CacheKeys {

    private CacheKeys() {
    }

    public static String pageHash(String search, Pageable pageable) {
        String q = search == null ? "" : search.trim().toLowerCase();
        return Integer.toHexString((q + "|" + pageablePart(pageable)).hashCode());
    }

    public static String pageHash(Long accountId, Object type, String search, Pageable pageable) {
        String q = search == null ? "" : search.trim().toLowerCase();
        String t = type == null ? "" : type.toString();
        String aid = accountId == null ? "" : accountId.toString();
        return Integer.toHexString((aid + "|" + t + "|" + q + "|" + pageablePart(pageable)).hashCode());
    }

    public static String rangeHash(Object from, Object to) {
        return Integer.toHexString((String.valueOf(from) + "|" + String.valueOf(to)).hashCode());
    }

    private static String pageablePart(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return "unpaged";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(pageable.getPageNumber()).append(':').append(pageable.getPageSize());
        Sort sort = pageable.getSort();
        if (sort != null && sort.isSorted()) {
            sort.forEach(o -> sb.append('|').append(o.getProperty()).append(',').append(o.getDirection()));
        }
        return sb.toString();
    }
}
