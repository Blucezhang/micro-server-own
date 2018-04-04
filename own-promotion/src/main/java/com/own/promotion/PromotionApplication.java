package com.own.promotion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Created by Bluce on 2018/4/4.
 */
@SpringBootApplication
@EnableAsync
@EnableHystrix
@EnableFeignClients
@ComponentScan("com.own.promotion")
public class PromotionApplication {

    public static void main(String[] args){
        SpringApplication.run(PromotionApplication.class,args);
    }
}
