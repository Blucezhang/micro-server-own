package com.own.send.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by Bluce on 2018/4/4.
 * 邮件，推送服务
 */
@SpringBootApplication
@EnableAsync
@EnableFeignClients
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.own.send.server","com.own.face"})
@EnableJpaRepositories(basePackages={"com.own.send.server.dao"})
public class SendServerApplication {

    public static void main(String[] args){
        SpringApplication.run(SendServerApplication.class,args);
    }

}
