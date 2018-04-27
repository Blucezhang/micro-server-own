package com.own.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EntityScan(basePackages = {"com.own.product.domain"})
@ComponentScan(basePackages = {"com.own.product","com.own.face"})
@EnableNeo4jRepositories(basePackages = {"com.own.product.dao"})
public class OwnProductApplication{

	public static void main(String[] args) {
		SpringApplication.run(OwnProductApplication.class, args);
	}
}
