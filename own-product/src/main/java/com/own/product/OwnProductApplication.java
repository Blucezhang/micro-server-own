package com.own.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableAsync
@EnableFeignClients
@ComponentScan(basePackages = {"com.own.product","com.own.face"})
@EnableNeo4jRepositories(basePackages = {"com.own.product.dao"})
public class OwnProductApplication{

	public static void main(String[] args) {
		SpringApplication.run(OwnProductApplication.class, args);
	}
}
