package com.own.common.config;


import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
@ConditionalOnProperty(name = {"spring.datasource.dynamic.enable"},matchIfMissing = false,havingValue = "true")
public class DSConfig {

    @Bean
    @Qualifier("createDSMaster")
    @ConfigurationProperties("spring.datasource.druid.master")
    public DataSource createDSMaster(){
        log.debug("master 路过");
        return DruidDataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    @Qualifier("createDSSlave")
    @ConfigurationProperties("spring.datasource.druid.slave")
    public DataSource createDSSlave(){
            log.debug("slave 路过");
        return DruidDataSourceBuilder.create().build();
    }



    @Primary
    @Bean
    public DataSource dataSource(){
        log.debug("addDBs 路过");
        DynamicDS datasource = new DynamicDS();
        DataSource master = this.createDSMaster();
        DataSource slave = this.createDSSlave();
        datasource.addDS("master",master);
        datasource.addDS("slave",slave);
        datasource.setDefaultTargetDataSource(master);
        return datasource;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource){
        log.debug("加入spring容器");
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource){
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setConfigLocation(new ClassPathResource("mybatis.cfg.xml"));
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            bean.setMapperLocations(resolver.getResources("classpath*:mapper/*Dao.xml"));
            return bean.getObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

}
