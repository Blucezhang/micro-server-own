package com.own.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.own.order.domain", "com.own.face.trade.idempotency"})
@EnableJpaRepositories(basePackages = {"com.own.order.repository", "com.own.face.trade.idempotency"})
@ComponentScan(basePackages = {"com.own.order", "com.own.face"})
@EnableScheduling
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Bean
    @Primary
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /** Webhooks are absolute operator URLs, not Eureka service names. */
    @Bean(name = "outboxRestTemplate")
    public RestTemplate outboxRestTemplate() {
        return new RestTemplate();
    }
}
