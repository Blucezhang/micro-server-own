package promotion.main;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.server.Neo4jServer;
import org.springframework.data.neo4j.server.RemoteServer;
import org.springframework.transaction.annotation.EnableTransactionManagement;



@Configuration  
@EnableAutoConfiguration
@EnableEurekaClient
@EnableTransactionManagement
@EnableNeo4jRepositories(basePackages="promotion.dao")
@ComponentScan(basePackages="promotion.dao,promotion.action,promotion.service")
@SpringBootApplication
@EnableEurekaServer
@EnableDiscoveryClient
public class PromotionMain extends Neo4jConfiguration{

    @Bean
	public SessionFactory getSessionFactory() {
		// TODO Auto-generated method stub
		return new SessionFactory("promotion.dao.domain");
	}

	@Bean
    public Neo4jServer neo4jServer() {
        return new RemoteServer("http://localhost:7474","neo4j1","neo4j123");
    }

	
	
	
	 public static  void main(String[] args){
	    	SpringApplication.run(PromotionMain.class, args);
	    	System.out.println("实例已启动================>");
	    }
	 
    
}
