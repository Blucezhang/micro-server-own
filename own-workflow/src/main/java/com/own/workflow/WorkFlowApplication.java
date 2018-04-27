package com.own.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by Bluce on 2018/4/4.
 */
@EnableAutoConfiguration
@EnableDiscoveryClient
@SpringBootApplication
@EntityScan(basePackages = {"com.own.workflow.domain","domain"})
@EnableJpaRepositories(basePackages = {"com.own.workflow.dao"})
@ComponentScan(basePackages = {"rdb.dao","com.own.workflow","com.own.face"})
public class WorkFlowApplication {
    public static void main(String[] args){
        SpringApplication.run(WorkFlowApplication.class,args);
    }
}
