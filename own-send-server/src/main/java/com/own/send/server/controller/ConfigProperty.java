package com.own.send.server.controller;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@EnableConfigurationProperties
@PropertySource("classpath:action.properties")
@Component("configProperty")
@Data
public class ConfigProperty {

    private String appKey;
    private String masterSecret;
    private String sn;
    private String serviceURL;
    private String pwd;
    private String host;
    private String auth;
    private String from;
    private String subject;
    private String title;
    private String user;
    private String password;

}
