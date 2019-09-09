package com.own.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Doerr
 * @title: FilterUrlsPropertiesConifg
 * @projectName github_own_P
 * @description: TODO
 * @date 2019/9/914:04
 */
@Configuration
@ConditionalOnExpression("!'${urls}'.isEmpty()")
@ConfigurationProperties(prefix = "urls")
public class FilterUrlsPropertiesConifg {

    private List<String> anon = new ArrayList<String>();

    public List<String> getAnon() {
        return anon;
    }

    public void setAnon(List<String> anon) {
        this.anon = anon;
    }
}
