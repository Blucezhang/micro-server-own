package com.own.promotion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by Bluce on 2018/4/4.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@EntityScan(basePackages = {"com.own.promotion.dao.domain", "com.own.promotion.coupon.domain", "com.own.face.trade.idempotency", "com.own.face.trade.inbox"})
@EnableNeo4jRepositories(basePackages = "com.own.promotion.dao")
@EnableJpaRepositories(basePackages = {"com.own.promotion.coupon.repository", "com.own.face.trade.idempotency", "com.own.face.trade.inbox"})
@ComponentScan(basePackages = {"com.own.promotion", "com.own.face"})
public class PromotionApplication {

    public static void main(String[] args){
        SpringApplication.run(PromotionApplication.class,args);
    }
}
