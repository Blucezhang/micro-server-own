package com.own.order;

import com.own.face.trade.config.TradeHttpProperties;
import com.own.face.trade.config.TradeRecoveryProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties({TradeHttpProperties.class, TradeRecoveryProperties.class})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Bean
    @Primary
    @LoadBalanced
    public RestTemplate restTemplate(
            TradeHttpProperties properties) {
        var requests = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        requests.setConnectTimeout(properties.connectTimeoutMillis());
        requests.setReadTimeout(properties.readTimeoutMillis());
        return new RestTemplate(requests);
    }

    @Bean(name = "tradeRecoveryScheduler")
    public org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler recoveryScheduler() {
        var scheduler = new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1); scheduler.setThreadNamePrefix("order-recovery-"); return scheduler;
    }
    @Bean(name = "taskScheduler")
    public org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler backgroundScheduler() {
        var scheduler = new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1); scheduler.setThreadNamePrefix("order-background-"); return scheduler;
    }

    /** Webhooks use absolute operator URLs and bypass service discovery. */
    @Bean(name = "outboxRestTemplate")
    public RestTemplate outboxRestTemplate() {
        return new RestTemplate();
    }
}
