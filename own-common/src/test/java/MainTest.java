import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@SpringBootApplication
@ImportResource({"classpath*:IocConf/Core.xml","classpath*:IocConf/Service.xml","classpath*:IocConf/Intercept.xml","classpath*:IocConf/SubFun.xml"})
@EnableAutoConfiguration
public class MainTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SpringApplication.run(MainTest.class,args);

	}
	
	@Bean
	public CrossFilter croFilter() {
		final CrossFilter crossFilter = new CrossFilter();
		return crossFilter;
	}
	
	@Bean
	public ConfigServerContainer configServerContainer() {
		final ConfigServerContainer configServerContainer = new ConfigServerContainer();
		return configServerContainer;
	}

}
