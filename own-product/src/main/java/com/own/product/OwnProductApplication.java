package com.own.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableHystrix
@EnableAsync
@EnableFeignClients
@ComponentScan("com.own.proudct")
public class OwnProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(OwnProductApplication.class, args);
	}
}
