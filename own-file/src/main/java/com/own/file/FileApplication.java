package com.own.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by Bluce on 2018/4/4.
 */
@SpringBootApplication
@ComponentScan("com.own.file")
@EnableFeignClients
@EnableHystrix
public class FileApplication {

    public static void main(String[] args){
        SpringApplication.run(FileApplication.class,args);
    }
}
