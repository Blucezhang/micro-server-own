package com.own.user.party.config;

import jakarta.persistence.EntityManagerFactory;
import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;

/** Graph grants and SQL account records must use their own transaction resources. */
@Configuration(proxyBeanMethods = false)
public class PersistenceTransactionConfig {
    @Bean
    @Primary
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Neo4jTransactionManager neo4jTransactionManager(Driver driver, DatabaseSelectionProvider databases) {
        return new Neo4jTransactionManager(driver, databases);
    }
}
