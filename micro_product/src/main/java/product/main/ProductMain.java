package product.main;

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
@ComponentScan(basePackages = "product.action")
@EnableAutoConfiguration
@EnableNeo4jRepositories(basePackages = "product.dao")
@EnableTransactionManagement
@SpringBootApplication
@EnableEurekaClient
public class ProductMain extends Neo4jConfiguration {
	
	
	
	  @Bean
	    public Neo4jServer neo4jServer() {
	        return new RemoteServer("http://localhost:7474","neo4j","hjd");
	    }

	    @Bean
	    public SessionFactory getSessionFactory() {
	        // with domain entity base package(s)
	        return new SessionFactory("product.dao.domain");
	    }

	    // needed for session in view in web-applications
	    @Bean
	    @Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
	    public Session getSession() throws Exception {
	        return super.getSession();
	    }
	    
	    @Bean
		public CrossFilter croFilter() {
			final CrossFilter crossFilter = new CrossFilter();
			return crossFilter;
		}
	    
	    public static  void main(String[] args){
	    	SpringApplication.run(ProductMain.class, args);
	    	ApplicationInfoManager.getInstance().setInstanceStatus(InstanceStatus.UP);
	    	System.out.println("end");
	    }

}
