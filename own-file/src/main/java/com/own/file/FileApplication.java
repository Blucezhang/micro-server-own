package com.own.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by Bluce on 2018/4/4.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.own.file.domain"})
@EnableJpaRepositories(basePackages = {"com.own.file.repository"})
@ComponentScan(basePackages = {"com.own.file","com.own.face"})
public class FileApplication {

    public static void main(String[] args){
        SpringApplication.run(FileApplication.class,args);
    }
}
