package com.own.eureka.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaRegistryAvailableEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
public class EurekaListener implements ApplicationListener{
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
    //服务注册，失效，心跳检测
        if(applicationEvent instanceof EurekaInstanceRegisteredEvent){ //注册
            EurekaInstanceRegisteredEvent eurekaInstanceRegisteredEvent = (EurekaInstanceRegisteredEvent) applicationEvent;
            log.info("服务-> {}",eurekaInstanceRegisteredEvent.getInstanceInfo().getAppName(),"注册成功了");
        }

        if(applicationEvent instanceof EurekaInstanceRenewedEvent){//心跳
            EurekaInstanceRenewedEvent eurekaInstanceRenewedEvent = (EurekaInstanceRenewedEvent) applicationEvent;
            log.info("心跳检测 ->{}",eurekaInstanceRenewedEvent.getInstanceInfo().getAppName());
        }

        if(applicationEvent instanceof EurekaRegistryAvailableEvent){
            EurekaRegistryAvailableEvent eurekaRegistryAvailableEvent = (EurekaRegistryAvailableEvent) applicationEvent;
            log.info("服务 注册时间-> {}", DateFormatUtils.format(eurekaRegistryAvailableEvent.getTimestamp(), "yyyy-MM-dd HH:mm:ss"));
        }

        if(applicationEvent instanceof EurekaInstanceCanceledEvent){//服务挂、不可用
            EurekaInstanceCanceledEvent eurekaInstanceCanceledEvent = (EurekaInstanceCanceledEvent) applicationEvent;
            // 获取当前Eureka实例中的节点信息
           /* PeerAwareInstanceRegistry registry = EurekaServerContextHolder.getInstance().getServerContext().getRegistry();
            Applications applications = registry.getApplications();
            // 遍历获取已注册节点中与当前失效节点ID一致的节点信息
            applications.getRegisteredApplications().forEach((registeredApplication) -> {
                registeredApplication.getInstances().forEach((instance) -> {
                    if (instance.getInstanceId().equals(eurekaInstanceCanceledEvent.getServerId())) {
                        log.debug("服务：" + instance.getAppName() + " 挂啦。。。");
                        // // TODO: 2017/9/3 扩展消息提醒 邮件、手机短信、微信等
                    }
                });
            });*/
        }
    }
}
