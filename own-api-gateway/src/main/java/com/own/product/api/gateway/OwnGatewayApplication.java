package com.own.product.api.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by Bluce on 2018/3/22.
 */
@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.own.product.api.gateway","com.own.face"})
public class OwnGatewayApplication {

    public static void main(String[] args){
        SpringApplication.run(OwnGatewayApplication.class,args);
    }
}
