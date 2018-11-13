package com.own.ownauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

@Slf4j
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.own.common.config","com.own.ownauth"})
@EnableAuthorizationServer
//@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class OwnAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(OwnAuthApplication.class, args);
	}

	@Bean(name = "passwordEncoder")
	@Primary
	public PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
}
