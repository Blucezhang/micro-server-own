package com.own.product;

import com.own.product.config.ProductPriceAuditProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import jakarta.persistence.EntityManagerFactory;
import org.neo4j.driver.Driver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@EnableConfigurationProperties(ProductPriceAuditProperties.class)
@EntityScan(basePackages = {"com.own.product.domain", "com.own.product.favorite.domain", "com.own.product.review.domain", "com.own.product.review.moderation.domain", "com.own.product.audit.domain", "com.own.face.trade.idempotency"})
@ComponentScan(basePackages = {"com.own.product","com.own.face"})
@EnableNeo4jRepositories(basePackages = {"com.own.product.dao"}, transactionManagerRef = "neo4jTransactionManager")
@EnableJpaRepositories(basePackages = {"com.own.product.favorite.repository", "com.own.product.review.repository", "com.own.product.review.moderation.repository", "com.own.product.audit.repository", "com.own.face.trade.idempotency"}, transactionManagerRef = "transactionManager")
public class OwnProductApplication{

	public static void main(String[] args) {
		SpringApplication.run(OwnProductApplication.class, args);
	}

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() { return new RestTemplate(); }

    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean(name = "neo4jTransactionManager")
    public PlatformTransactionManager neo4jTransactionManager(Driver driver) {
        return new Neo4jTransactionManager(driver);
    }
}
