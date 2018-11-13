package com.own.owneureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class OwnEurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(OwnEurekaApplication.class, args);
	}
}
