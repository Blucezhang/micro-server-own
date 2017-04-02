package face.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import face.ems.EmsFace;
import face.order.OrderFace;
import face.party.PartyFace;
import face.product.ProductFace;
import face.promotion.PromotionFace;
import face.settlement.SettleFace;
import face.workflow.WorkFlowFace;

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