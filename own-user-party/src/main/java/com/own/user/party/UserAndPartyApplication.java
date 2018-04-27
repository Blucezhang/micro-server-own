package com.own.user.party;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Created by Bluce on 2018/4/4.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.own.user.party.dao.domain"})
@ComponentScan(basePackages = {"com.own.user.party","com.own.face"})
@EnableFeignClients
@EnableNeo4jRepositories(basePackages = {"com.own.user.party.dao"})
public class UserAndPartyApplication {

    public static void main(String[] args){
        SpringApplication.run(UserAndPartyApplication.class,args);
    }
}
