package com.bankone.replica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Ensures replica has schema (pg_dump --schema-only) when empty, then runs an initial sync.
 */
@Component
@Order(50)
@ConditionalOnProperty(prefix = "app.replica", name = "enabled", havingValue = "true")
public class ReplicaBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ReplicaBootstrap.class);

    private final DataSource writeDataSource;
    private final DataSource readDataSource;
    private final ReplicaProperties props;
    private final ReplicaSyncService syncService;

    public ReplicaBootstrap(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource,
            ReplicaProperties props,
            ReplicaSyncService syncService
    ) {
        this.writeDataSource = writeDataSource;
        this.readDataSource = readDataSource;
        this.props = props;
        this.syncService = syncService;
    }

    @Override
    public void run(ApplicationArguments args) {
        Thread t = new Thread(() -> {
            try {
                if (replicaTableCount() == 0) {
                    log.info("Replica schema empty — attempting pg_dump schema copy");
                    copySchemaWithPgDump();
                }
                syncService.syncNow();
            } catch (Exception ex) {
                log.warn("Replica bootstrap/sync skipped or failed: {}", ex.getMessage());
            }
        }, "replica-bootstrap");
        t.setDaemon(true);
        t.start();
    }

    private int replicaTableCount() {
        JdbcTemplate read = new JdbcTemplate(readDataSource);
        Integer n = read.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
                """,
                Integer.class
        );
        return n == null ? 0 : n;
    }

    private void copySchemaWithPgDump() throws Exception {
        ParsedJdbc write = parseJdbc(connectionUrl(writeDataSource));
        ParsedJdbc read = parseJdbc(props.getReadUrl());

        ProcessBuilder dump = new ProcessBuilder(
                "pg_dump",
                "-h", write.host(),
                "-p", String.valueOf(write.port()),
                "-U", props.getUsername(),
                "-d", write.database(),
                "--schema-only",
                "--no-owner",
                "--no-acl"
        );
        dump.environment().put("PGPASSWORD", props.getPassword());
        dump.redirectErrorStream(true);

        ProcessBuilder restore = new ProcessBuilder(
                "psql",
                "-h", read.host(),
                "-p", String.valueOf(read.port()),
                "-U", props.getUsername(),
                "-d", read.database(),
                "-v", "ON_ERROR_STOP=1"
        );
        restore.environment().put("PGPASSWORD", props.getPassword());
        restore.redirectErrorStream(true);

        Process dumpProc = dump.start();
        Process restoreProc = restore.start();

        try (var in = dumpProc.getInputStream();
             var out = restoreProc.getOutputStream();
             var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("\\restrict") || line.startsWith("\\unrestrict")) {
                    continue;
                }
                out.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            }
            out.flush();
        }
        restoreProc.getOutputStream().close();

        if (!dumpProc.waitFor(120, TimeUnit.SECONDS) || dumpProc.exitValue() != 0) {
            throw new IllegalStateException("pg_dump failed exit=" + dumpProc.exitValue());
        }
        if (!restoreProc.waitFor(120, TimeUnit.SECONDS) || restoreProc.exitValue() != 0) {
            String err = new String(restoreProc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new IllegalStateException("psql restore failed: " + err);
        }
        log.info("Replica schema copied via pg_dump");
    }

    private static String connectionUrl(DataSource ds) throws Exception {
        try (var c = ds.getConnection()) {
            return c.getMetaData().getURL();
        }
    }

    private static ParsedJdbc parseJdbc(String jdbcUrl) {
        String stripped = jdbcUrl.startsWith("jdbc:") ? jdbcUrl.substring(5) : jdbcUrl;
        URI uri = URI.create(stripped);
        String host = uri.getHost() != null ? uri.getHost() : "localhost";
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String path = uri.getPath() != null ? uri.getPath() : "/bankone";
        String db = path.startsWith("/") ? path.substring(1) : path;
        int q = db.indexOf('?');
        if (q >= 0) {
            db = db.substring(0, q);
        }
        return new ParsedJdbc(host, port, db);
    }

    private record ParsedJdbc(String host, int port, String database) {}
}
