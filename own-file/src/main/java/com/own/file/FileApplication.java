package com.own.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by Bluce on 2018/4/4.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.own.file.domain"})
@ComponentScan(basePackages = {"com.own.file"})
public class FileApplication {

    public static void main(String[] args){
        SpringApplication.run(FileApplication.class,args);
    }
}
