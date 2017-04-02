package File.Main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;


@org.springframework.boot.context.properties.EnableConfigurationProperties({Config.class})
@SpringBootApplication
@EnableEurekaClient
public class FileMain {
	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(FileMain.class);
		app.run(FileMain.class, args);
		System.out.println("FileMain启动了...");
	}


	@Bean
	public CrossFilter crossFilter() {
		CrossFilter corCrossFilter = new CrossFilter();
		return corCrossFilter;
	}

}
