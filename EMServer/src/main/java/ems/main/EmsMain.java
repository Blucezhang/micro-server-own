package ems.main;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;



@Configuration  
@EnableAutoConfiguration
@EnableEurekaClient
@EnableTransactionManagement
@EnableJpaRepositories(basePackages="ems.dao")
@ComponentScan(basePackages="ems.dao,ems.action,ems.service")
@SpringBootApplication
@EnableEurekaServer
@EnableDiscoveryClient
public class EmsMain{

	 @Bean
	 public BasicDataSource dataSource() {
	    BasicDataSource data = new BasicDataSource();//设置数据库参数
	    data.setDriverClassName("com.mysql.jdbc.Driver");
	    data.setUrl("jdbc:mysql://localhost:3306/Ems?useUnicode=true&characterEncoding=utf8");
	    data.setUsername("root");
	    data.setPassword("");
	    data.setValidationQuery("select * from Sms");
	    data.setNumTestsPerEvictionRun(8);
	    data.setTimeBetweenEvictionRunsMillis(600000);
	    data.setTestOnBorrow(false);
	    data.setTestWhileIdle(true);
	    
	    return data;
	  }
	 
	
	 
	 
	 
	 
	 
	 @Bean
	 public EntityManagerFactory entityManagerFactory() {

	    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
	    vendorAdapter.setGenerateDdl(true);

	    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
	    factory.setJpaVendorAdapter(vendorAdapter);
	    factory.setPackagesToScan("ems.domain");//扫描实体entity
	    
	    Properties jp = new Properties();
	    jp.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
	    jp.setProperty("hibernate.show_sql", "true");
	    jp.setProperty("hibernate.format_sql", "true");
	    factory.setJpaProperties(jp);
	    
	    factory.setDataSource(dataSource());
	    factory.afterPropertiesSet();
	    factory.setJpaDialect(new HibernateJpaDialect());
	    
	    return factory.getObject();
	  }
	 
	 @Bean
	 public PlatformTransactionManager transactionManager() {

	    JpaTransactionManager txManager = new JpaTransactionManager();
	    txManager.setEntityManagerFactory(entityManagerFactory());
	    
	    return txManager;
	  }
	 
	 @Bean
	 public BeanNameUrlHandlerMapping beanNameUrlHandlerMapping(){
		 return new BeanNameUrlHandlerMapping();
	 }
	 
	 @Bean
	 public MessageSource messageSource(){
		 MessageSource messageSource = new ResourceBundleMessageSource();
		 return messageSource;
		 
	 }

    public static  void main(String[] args){
    	SpringApplication.run(EmsMain.class, args);
    	System.out.println("实例已启动================>");
    }
    
}