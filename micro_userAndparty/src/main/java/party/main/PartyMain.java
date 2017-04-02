package party.main;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.server.Neo4jServer;
import org.springframework.data.neo4j.server.RemoteServer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;

@Configuration  
@ComponentScan(basePackages = "party.action")
@EnableAutoConfiguration
@EnableNeo4jRepositories(basePackages = "party.dao")
@EnableTransactionManagement
@SpringBootApplication
@EnableEurekaClient
public class PartyMain extends Neo4jConfiguration {

    @Bean
    public Neo4jServer neo4jServer() {
        return new RemoteServer("http://192.168.0.104:7474","neo4j","hjd");
    }

    @Bean
    public SessionFactory getSessionFactory() {
        // with domain entity base package(s)
        return new SessionFactory("party.dao.domain");
    }

    // needed for session in view in web-applications
    @Bean
    @Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Session getSession() throws Exception {
        return super.getSession();
    }
    
    
    public static  void main(String[] args){
    	SpringApplication.run(PartyMain.class, args);
    	System.out.println("PartyMain启动了！");
    }
    
    @Bean
    public CrossFilter crossFilter(){
    	CrossFilter corCrossFilter = new CrossFilter();
    	return corCrossFilter;
    }
    
    

}