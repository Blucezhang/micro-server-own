package com.own.common.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Doerr
 * @title: SwaggerConfig
 * @projectName github_own_P
 * @description: TODO
 * @date 2019/9/914:08
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Swagger API ")
                .description("http://proudkids.cn")
                .termsOfServiceUrl("https://zyplyer.cn")
                // .contact(new Contact("Jones","git@60.205.210.36:proud-server/proudkids_ems.git","zzq223@163.com"))
                .version("2.0")
                .build();
    }

    @Bean
    public Docket createRestApi() {
        ParameterBuilder tokenBuilder = new ParameterBuilder();
        List<Parameter> parameterList = new ArrayList<Parameter>();
        tokenBuilder.name("Authorization")
                .defaultValue("去其他请求中获取heard中token参数")
                .description("令牌")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(true).build();
        parameterList.add(tokenBuilder.build());
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(parameterList);
    }


}
