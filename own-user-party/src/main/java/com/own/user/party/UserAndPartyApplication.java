package com.own.user.party;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Created by Bluce on 2018/4/4.
 */
@SpringBootApplication
@ComponentScan("com.own.user.party")
@EnableAsync
@EnableFeignClients
public class UserAndPartyApplication {

    public static void main(String[] args){
        SpringApplication.run(UserAndPartyApplication.class,args);
    }
}
