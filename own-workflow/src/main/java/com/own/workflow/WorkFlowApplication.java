package com.own.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by Bluce on 2018/4/4.
 */
@EnableAutoConfiguration
@EnableDiscoveryClient
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = com.own.workflow.dao.RdbBaseDao.class)
@ComponentScan(basePackages = {"com.own.workflow"})
public class WorkFlowApplication {
    public static void main(String[] args){
        SpringApplication.run(WorkFlowApplication.class,args);
    }
}
