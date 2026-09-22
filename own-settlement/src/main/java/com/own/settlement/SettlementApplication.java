package com.own.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.own.settlement.domain", "com.own.face.trade.idempotency"})
@EnableJpaRepositories(basePackages = {"com.own.settlement.repository", "com.own.face.trade.idempotency"})
@ComponentScan(basePackages = {"com.own.settlement", "com.own.face"})
@EnableScheduling
public class SettlementApplication {
    public static void main(String[] args) { SpringApplication.run(SettlementApplication.class, args); }
    @Bean @LoadBalanced public RestTemplate restTemplate() { return new RestTemplate(); }
}
