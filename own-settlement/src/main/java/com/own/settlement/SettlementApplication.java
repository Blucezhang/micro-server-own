package com.own.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.own.settlement.domain", "com.own.face.trade.idempotency", "com.own.face.trade.inbox"})
@EnableJpaRepositories(basePackages = {"com.own.settlement.repository", "com.own.face.trade.idempotency", "com.own.face.trade.inbox"})
@ComponentScan(basePackages = {"com.own.settlement", "com.own.face"})
@EnableScheduling
public class SettlementApplication {
    public static void main(String[] args) { SpringApplication.run(SettlementApplication.class, args); }
    @Bean @LoadBalanced public RestTemplate restTemplate(
            @org.springframework.beans.factory.annotation.Value("${trade.http.connect-timeout-millis:3000}") int connectMillis,
            @org.springframework.beans.factory.annotation.Value("${trade.http.read-timeout-millis:30000}") int readMillis) {
        if (connectMillis < 1 || connectMillis > 60_000 || readMillis < 1 || readMillis > 60_000) throw new IllegalArgumentException("HTTP timeouts must be 1..60000 ms");
        var requests = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        requests.setConnectTimeout(connectMillis); requests.setReadTimeout(readMillis);
        return new RestTemplate(requests);
    }
    @Bean(name = "tradeRecoveryScheduler")
    public org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler recoveryScheduler() {
        var scheduler = new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1); scheduler.setThreadNamePrefix("settlement-recovery-"); return scheduler;
    }
    @Bean(name = "taskScheduler")
    public org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler backgroundScheduler() {
        var scheduler = new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1); scheduler.setThreadNamePrefix("settlement-background-"); return scheduler;
    }
}
