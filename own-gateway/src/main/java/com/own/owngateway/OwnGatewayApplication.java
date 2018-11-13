package com.own.owngateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableZuulProxy
@SpringBootApplication
@ComponentScan(basePackages = {"com.own.owngateway.**.*"})
public class OwnGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(OwnGatewayApplication.class, args);
	}
}
