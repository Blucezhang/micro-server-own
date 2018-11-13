package com.own.owncommon.config;


import com.own.owncommon.ems.EmsFace;
import com.own.owncommon.order.OrderFace;
import com.own.owncommon.party.PartyFace;
import com.own.owncommon.product.ProductFace;
import com.own.owncommon.promotion.PromotionFace;
import com.own.owncommon.settlement.SettleFace;
import com.own.owncommon.workflow.WorkFlowFace;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableDiscoveryClient
public class FaceConfig {

    @Bean
    public PartyFace getOrgFace() {
        return new PartyFace();
    }
    
    @Bean 
    public EmsFace getEmsFace(){
    	return new EmsFace();
    }
    
    @Bean
    public WorkFlowFace getWorkFlowFace(){
    	return new WorkFlowFace();
    }
    
    @Bean 
    public ProductFace getProductFace(){
    	return new ProductFace();
    }
    
    @Bean 
    public OrderFace getOrderFace(){
    	return new OrderFace();
    }
    
    @Bean 
    public SettleFace getSettleFace(){
    	return new SettleFace();
    }
    
    @Bean
    public PromotionFace getPromotionFace(){
    	return new PromotionFace();
    }
   
  }