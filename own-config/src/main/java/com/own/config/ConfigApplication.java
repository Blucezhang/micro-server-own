package com.own.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Created by Bluce on 2018/3/15.
 */
@SpringBootApplication
@EnableAutoConfiguration
@EnableDiscoveryClient
public class ConfigApplication {

    public static void main(String[] args){
        SpringApplication.run(ConfigApplication.class,args);
    }
}
