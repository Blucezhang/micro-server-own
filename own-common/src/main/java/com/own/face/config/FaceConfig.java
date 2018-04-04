package com.own.face.config;

import com.own.face.ems.EmsFace;
import com.own.face.order.OrderFace;
import com.own.face.party.PartyFace;
import com.own.face.product.ProductFace;
import com.own.face.promotion.PromotionFace;
import com.own.face.settlement.SettleFace;
import com.own.face.workflow.WorkFlowFace;
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