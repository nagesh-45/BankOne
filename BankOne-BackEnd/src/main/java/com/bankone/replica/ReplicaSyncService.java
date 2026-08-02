package com.bankone.replica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Copies core tables from write DB → read replica (truncate + insert).
 * Sync uses the physical pools directly (not the routing DataSource).
 */
@Service
@ConditionalOnProperty(prefix = "app.replica", name = "enabled", havingValue = "true")
public class ReplicaSyncService {

    private static final Logger log = LoggerFactory.getLogger(ReplicaSyncService.class);

    /** Parent tables before children (FK-safe insert order). */
    static final List<String> CORE_TABLES = List.of(
            "roles",
            "role_access",
            "users",
            "user_roles",
            "customers",
            "account_policy",
            "account",
            "bank_transaction",
            "beneficiary",
            "transfer_request"
    );

    private static final String AUDIT_TABLE = "audit_event";

    private final DataSource writeDataSource;
    private final DataSource readDataSource;
    private final ReplicaProperties props;
    private final AtomicReference<Instant> lastSyncAt = new AtomicReference<>();
    private final AtomicReference<String> lastSyncMessage = new AtomicReference<>("never");
    private final AtomicReference<Map<String, Long>> lastRowCounts =
            new AtomicReference<>(Collections.emptyMap());

    public ReplicaSyncService(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource,
            ReplicaProperties props
    ) {
        this.writeDataSource = writeDataSource;
        this.readDataSource = readDataSource;
        this.props = props;
    }

    public synchronized ReplicaSyncStatus syncNow() {
        Instant started = Instant.now();
        Map<String, Long> counts = new LinkedHashMap<>();
        try {
            List<String> existing = tablesPresentOnPrimary();
            List<String> toSync = new ArrayList<>(CORE_TABLES.stream().filter(existing::contains).toList());
            if (props.isIncludeAudit() && existing.contains(AUDIT_TABLE)) {
                toSync.add(AUDIT_TABLE);
            }
            if (toSync.isEmpty()) {
                throw new IllegalStateException("No sync tables found on primary");
            }

            try (Connection read = readDataSource.getConnection()) {
                read.setAutoCommit(false);
                truncateAll(read, toSync);
                for (String table : toSync) {
                    long n = copyTable(table, read);
                    counts.put(table, n);
                }
                read.commit();
            }

            lastSyncAt.set(started);
            String msg = "ok tables=" + counts.size();
            lastSyncMessage.set(msg);
            lastRowCounts.set(counts);
            log.info("Replica sync completed in {} ms: {}",
                    Instant.now().toEpochMilli() - started.toEpochMilli(), counts);
            return status();
        } catch (Exception ex) {
            lastSyncMessage.set("failed: " + ex.getMessage());
            log.error("Replica sync failed", ex);
            throw new IllegalStateException("Replica sync failed: " + ex.getMessage(), ex);
        }
    }

    public ReplicaSyncStatus status() {
        return new ReplicaSyncStatus(
                lastSyncAt.get(),
                lastSyncMessage.get(),
                lastRowCounts.get()
        );
    }

    private List<String> tablesPresentOnPrimary() {
        JdbcTemplate write = new JdbcTemplate(writeDataSource);
        List<String> names = write.queryForList(
                """
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
                """,
                String.class
        );
        return names.stream().map(String::toLowerCase).toList();
    }

    private void truncateAll(Connection read, List<String> tables) throws Exception {
        String joined = String.join(", ", tables);
        try (Statement st = read.createStatement()) {
            st.execute("TRUNCATE TABLE " + joined + " RESTART IDENTITY CASCADE");
        }
    }

    private long copyTable(String table, Connection read) throws Exception {
        long count = 0;
        try (Connection write = writeDataSource.getConnection();
             Statement select = write.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            select.setFetchSize(500);
            try (ResultSet rs = select.executeQuery("SELECT * FROM " + table)) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                List<String> colNames = new ArrayList<>(cols);
                for (int i = 1; i <= cols; i++) {
                    colNames.add(meta.getColumnName(i));
                }
                String placeholders = String.join(", ", Collections.nCopies(cols, "?"));
                String colList = String.join(", ", colNames);
                String sql = "INSERT INTO " + table + " (" + colList + ") VALUES (" + placeholders + ")";
                try (PreparedStatement insert = read.prepareStatement(sql)) {
                    while (rs.next()) {
                        for (int i = 1; i <= cols; i++) {
                            Object value = rs.getObject(i);
                            if (value instanceof Timestamp ts) {
                                insert.setObject(i, ts);
                            } else {
                                insert.setObject(i, value);
                            }
                        }
                        insert.addBatch();
                        count++;
                        if (count % 500 == 0) {
                            insert.executeBatch();
                        }
                    }
                    insert.executeBatch();
                }
            }
        }
        return count;
    }
}
