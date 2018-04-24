package com.own.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by Bluce on 2018/4/4.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.own.workflow.server","com.own.workflow.controller","com.own.workflow.domain"})
@EnableJpaRepositories(basePackages = {"com.own.workflow.dao"})
@EnableHystrix
public class WorkFlowApplication {

    public static void main(String[] args){
        SpringApplication.run(WorkFlowApplication.class,args);
    }
}
