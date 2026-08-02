package com.bankone.sharding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Hibernate ddl-auto only touches the default shard (S0) at EMF bootstrap.
 * Ensure {@code shard_lab_customer} exists on every shard DB.
 */
@Component
@ConditionalOnProperty(prefix = "app.sharding", name = "enabled", havingValue = "true")
public class ShardSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ShardSchemaInitializer.class);

    private static final String DDL = """
            CREATE TABLE IF NOT EXISTS shard_lab_customer (
                id UUID PRIMARY KEY,
                full_name VARCHAR(120) NOT NULL,
                email VARCHAR(180) NOT NULL,
                created_at TIMESTAMPTZ NOT NULL
            )
            """;

    private final DataSource s0;
    private final DataSource s1;

    public ShardSchemaInitializer(
            @Qualifier("shard0DataSource") DataSource s0,
            @Qualifier("shard1DataSource") DataSource s1
    ) {
        this.s0 = s0;
        this.s1 = s1;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ensureSchema(s0, "s0");
        ensureSchema(s1, "s1");
    }

    private void ensureSchema(DataSource ds, String label) throws Exception {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            st.execute(DDL);
            log.info("Sharding lab schema ready on {}", label);
        }
    }
}
