package workflow.main;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import workflow.dao.RdbBaseDao;
 

@ComponentScan(basePackages = "workflow.service,workflow.action,workflow.domain")
@EnableEurekaServer
@EnableJpaRepositories(basePackages="workflow.dao") 
@SpringBootApplication
public class WorkMain {

	public static void main(String[] args) {
		SpringApplication.run(WorkMain.class, args);
		System.out.println("WorkMain 启动啦");
	}
	
	@Bean
	public RdbBaseDao getBaseDao(){
		return new RdbBaseDao();
	}
	

	@Bean
	public CrossFilter croFilter() {
		final CrossFilter crossFilter = new CrossFilter();
		return crossFilter;
	}

	@Bean
	public DataSource getSource() {
		BasicDataSource source = new org.apache.commons.dbcp.BasicDataSource();
		source.setDriverClassName("com.mysql.jdbc.Driver");
		source.setUrl("jdbc:mysql://124.205.89.213:3306/workflow?useUnicode=true&characterEncoding=utf8");
		source.setUsername("hjd");
		source.setPassword("hjd213");
		source.setValidationQuery("select 1");
		source.setNumTestsPerEvictionRun(8);
		source.setTimeBetweenEvictionRunsMillis(600000);
		source.setTestOnBorrow(false);
		source.setTestWhileIdle(true);

		return source;
	}
	  @Bean
	  public EntityManagerFactory getEntityManagerFactory() {

		  HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		    vendorAdapter.setGenerateDdl(true);

		    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		    factory.setJpaVendorAdapter(vendorAdapter);
		    factory.setPackagesToScan("workflow.domain");
		    
		    Properties properties = new Properties();
		    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
		    properties.setProperty("hibernate.show_sql", "true");
		    properties.setProperty("hibernate.format_sql", "true");
		    factory.setJpaProperties(properties);
		    
		    factory.setDataSource(getSource());
		    factory.afterPropertiesSet();
		    factory.setJpaDialect(new HibernateJpaDialect());
		    
		    return factory.getObject();
	  }

	  @Bean
	  public PlatformTransactionManager transactionManager() {
	    JpaTransactionManager txManager = new JpaTransactionManager();
	    txManager.setEntityManagerFactory(getEntityManagerFactory());
	    return txManager;
	  }
 	 
}