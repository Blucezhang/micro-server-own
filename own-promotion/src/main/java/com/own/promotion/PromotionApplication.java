package com.own.promotion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Created by Bluce on 2018/4/4.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EntityScan(basePackages = "com.own.promotion.dao.domain")
@EnableNeo4jRepositories(basePackages = "com.own.promotion.dao")
@ComponentScan(basePackages = {"com.own.promotion.controller"})
public class PromotionApplication {

    public static void main(String[] args){
        SpringApplication.run(PromotionApplication.class,args);
    }
}
