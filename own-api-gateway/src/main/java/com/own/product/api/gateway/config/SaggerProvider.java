package com.own.product.api.gateway.config;

import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bluce on 2018/4/26.
 */
@Primary
@Component
public class SaggerProvider  implements SwaggerResourcesProvider {

    private final RouteLocator routeLocator;

    public SaggerProvider(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        //从路由回去所有节点的服务名称
       routeLocator.getRoutes().forEach(k->{
           SwaggerResource swaggerResource = new SwaggerResource();
           swaggerResource.setName(k.getId());
           swaggerResource.setLocation(k.getFullPath().replace("**","v2/api-docs"));
           swaggerResource.setSwaggerVersion("1.0");
           resources.add(swaggerResource);
       });
       return resources;
    }

}
