package com.own.product.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Created by Bluce on 2018/3/15.
 */

@EnableConfigServer
@SpringBootApplication
public class ConfigApplication {

    public static void main(String[] args){
        SpringApplication.run(ConfigApplication.class,args);
    }
}
