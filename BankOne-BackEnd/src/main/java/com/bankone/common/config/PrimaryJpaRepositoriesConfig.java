package com.bankone.common.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Explicit primary EMF. Required because defining a second
 * {@link LocalContainerEntityManagerFactoryBean} for the sharding lab disables
 * Spring Boot's auto-configured primary EMF.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.bankone",
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.bankone\\.sharding\\.lab\\..*"
        ),
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class PrimaryJpaRepositoriesConfig {

    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            DataSource dataSource
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        return builder
                .dataSource(dataSource)
                .packages(
                        "com.bankone.account.entity",
                        "com.bankone.customer.entity",
                        "com.bankone.user.entity",
                        "com.bankone.role.entity",
                        "com.bankone.transaction.entity",
                        "com.bankone.audit.entity",
                        "com.bankone.transfer.entity",
                        "com.bankone.beneficiary.entity",
                        "com.bankone.common.entity"
                )
                .persistenceUnit("bankone")
                .properties(props)
                .build();
    }

    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory emf
    ) {
        return new JpaTransactionManager(emf);
    }
}
