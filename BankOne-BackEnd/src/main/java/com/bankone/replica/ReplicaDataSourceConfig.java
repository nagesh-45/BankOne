package com.bankone.replica;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * When replica is off: expose write pool as {@code @Primary} dataSource.
 * When replica is on: routing DS is primary (readOnly tx → bankone_read).
 */
@Configuration
@EnableConfigurationProperties(ReplicaProperties.class)
public class ReplicaDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(ReplicaDataSourceConfig.class);

    @Bean(name = "dataSource")
    @Primary
    @ConditionalOnProperty(prefix = "app.replica", name = "enabled", havingValue = "false", matchIfMissing = true)
    public DataSource primaryWriteOnlyDataSource(@Qualifier("writeDataSource") DataSource writeDataSource) {
        log.info("Replica disabled — using write DataSource as primary");
        return writeDataSource;
    }

    @Bean(name = "readDataSource")
    @ConditionalOnProperty(prefix = "app.replica", name = "enabled", havingValue = "true")
    public DataSource readDataSource(ReplicaProperties props) {
        HikariDataSource ds = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(props.getReadUrl())
                .username(props.getUsername())
                .password(props.getPassword())
                .driverClassName("org.postgresql.Driver")
                .build();
        ds.setPoolName("bankone-read");
        ds.setMaximumPoolSize(5);
        ds.setConnectionTimeout(30_000);
        ds.setReadOnly(false);
        log.info("Configuring read DataSource jdbcUrl={}", props.getReadUrl());
        return ds;
    }

    @Bean(name = "dataSource")
    @Primary
    @ConditionalOnProperty(prefix = "app.replica", name = "enabled", havingValue = "true")
    public DataSource primaryRoutingDataSource(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource
    ) {
        Map<Object, Object> targets = new HashMap<>();
        targets.put(DataSourceType.WRITE, writeDataSource);
        targets.put(DataSourceType.READ, readDataSource);

        ReplicationRoutingDataSource routing = new ReplicationRoutingDataSource();
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(writeDataSource);
        routing.afterPropertiesSet();
        log.info("Replica enabled — routing DataSource is primary (readOnly → READ)");
        return new LazyConnectionDataSourceProxy(routing);
    }
}
