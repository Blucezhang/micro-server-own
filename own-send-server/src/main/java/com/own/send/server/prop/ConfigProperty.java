package com.own.send.server.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "ems")
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
