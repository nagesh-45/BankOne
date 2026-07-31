package com.bankone.audit.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class AuditBackfillResult {

    private final Map<String, Integer> insertedBySource = new LinkedHashMap<>();
    private int skipped;
    private int inserted;

    public void addInserted(String source, int count) {
        insertedBySource.put(source, count);
        inserted += count;
    }

    public void addSkipped(int count) {
        skipped += count;
    }

    public Map<String, Integer> getInsertedBySource() {
        return insertedBySource;
    }

    public int getSkipped() {
        return skipped;
    }

    public int getInserted() {
        return inserted;
    }
}
