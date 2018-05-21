import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;

/**
 * spring boot 端口设置
 */
public class ConfigServerContainer implements EmbeddedServletContainerCustomizer{

    @Override  
    public void customize(ConfigurableEmbeddedServletContainer container) {
    	container.setPort(8080);  
        container.setSessionTimeout(15);
    }
	
}
