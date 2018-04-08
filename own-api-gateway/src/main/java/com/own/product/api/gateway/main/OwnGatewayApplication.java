package com.own.product.api.gateway.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * Created by Bluce on 2018/3/22.
 */
@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
public class OwnGatewayApplication {

    public static void main(String[] args){
        SpringApplication.run(OwnGatewayApplication.class,args);
    }
}
