package com.own.product.api.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by Bluce on 2018/3/22.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.own.product.api.gateway","com.own.face"})
public class OwnGatewayApplication {

    public static void main(String[] args){
        SpringApplication.run(OwnGatewayApplication.class,args);
    }
}
