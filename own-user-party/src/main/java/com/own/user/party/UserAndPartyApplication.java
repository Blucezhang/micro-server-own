package com.own.user.party;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by Bluce on 2018/4/4.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.own.user.party.dao.domain", "com.own.user.party.address.domain", "com.own.user.party.auth.domain", "com.own.face.trade.idempotency"})
@ComponentScan(basePackages = {"com.own.user.party","com.own.face"})
@EnableFeignClients
@EnableNeo4jRepositories(basePackages = {"com.own.user.party.dao"})
@EnableJpaRepositories(basePackages = {"com.own.user.party.address.repository", "com.own.user.party.auth.repository", "com.own.face.trade.idempotency"})
@EnableScheduling
public class UserAndPartyApplication {

    public static void main(String[] args){
        SpringApplication.run(UserAndPartyApplication.class,args);
    }
}
