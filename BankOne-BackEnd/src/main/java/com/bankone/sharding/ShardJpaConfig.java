package com.bankone.sharding;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Separate EMF + routing DataSource for the sharding lab only.
 * Primary bank DataSource / EMF stay untouched.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.sharding", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ShardingProperties.class)
@EnableJpaRepositories(
        basePackages = "com.bankone.sharding.lab",
        entityManagerFactoryRef = "shardEntityManagerFactory",
        transactionManagerRef = "shardTransactionManager"
)
public class ShardJpaConfig {

    @Bean
    public ShardRouter shardRouter(ShardingProperties properties) {
        return new ShardRouter(properties);
    }

    @Bean(name = "shard0DataSource")
    public DataSource shard0DataSource(ShardingProperties props) {
        return hikari(props.getS0Url(), props.getUsername(), props.getPassword(), "shard-s0");
    }

    @Bean(name = "shard1DataSource")
    public DataSource shard1DataSource(ShardingProperties props) {
        return hikari(props.getS1Url(), props.getUsername(), props.getPassword(), "shard-s1");
    }

    @Bean(name = "shardRoutingDataSource")
    public DataSource shardRoutingDataSource(
            @Qualifier("shard0DataSource") DataSource s0,
            @Qualifier("shard1DataSource") DataSource s1
    ) {
        Map<Object, Object> targets = new HashMap<>();
        targets.put(ShardId.S0, s0);
        targets.put(ShardId.S1, s1);

        AbstractRoutingDataSource routing = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                ShardId current = ShardContext.get();
                // Bootstrap / ddl-auto runs with no context — use S0 as default.
                return current != null ? current : ShardId.S0;
            }
        };
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(s0);
        routing.afterPropertiesSet();
        return routing;
    }

    @Bean(name = "shardEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean shardEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("shardRoutingDataSource") DataSource shardRoutingDataSource
    ) {
        return builder
                .dataSource(shardRoutingDataSource)
                .packages("com.bankone.sharding.lab")
                .persistenceUnit("shardLab")
                .properties(Map.of(
                        "hibernate.hbm2ddl.auto", "update",
                        "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"
                ))
                .build();
    }

    @Bean(name = "shardTransactionManager")
    public PlatformTransactionManager shardTransactionManager(
            @Qualifier("shardEntityManagerFactory") EntityManagerFactory emf
    ) {
        return new JpaTransactionManager(emf);
    }

    @Bean(name = "shardTransactionTemplate")
    public TransactionTemplate shardTransactionTemplate(
            @Qualifier("shardTransactionManager") PlatformTransactionManager tm
    ) {
        return new TransactionTemplate(tm);
    }

    private static DataSource hikari(String url, String user, String password, String poolName) {
        HikariDataSource ds = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(url)
                .username(user)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
        ds.setPoolName(poolName);
        ds.setMaximumPoolSize(3);
        ds.setConnectionTimeout(15_000);
        return ds;
    }
}
