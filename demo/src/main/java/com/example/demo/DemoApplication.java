package com.example.demo;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.AbstractEventNotifier;
import de.codecentric.boot.admin.server.notify.CompositeNotifier;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.RemindingNotifier;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@ComponentScan(basePackages = {"com.own.common.config","com.example.demo"})
@EnableAutoConfiguration
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@MapperScan("com.example.demo.dao.*")
@EnableDiscoveryClient
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
	@Profile("insecure")
	@Configuration
	public static class SecurityPermitAllConfig extends WebSecurityConfigurerAdapter {
		private final String adminContextPath;

		public SecurityPermitAllConfig(AdminServerProperties adminServerProperties) {
			this.adminContextPath = adminServerProperties.getContextPath();
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.authorizeRequests()
					.anyRequest()
					.permitAll()
					.and()
					.csrf()
					.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
					.ignoringAntMatchers(adminContextPath + "/instances", adminContextPath + "/actuator/**");
		}
	}

	@Profile("secure")
	// tag::configuration-spring-security[]
	@Configuration
	public static class SecuritySecureConfig extends WebSecurityConfigurerAdapter {
		private final String adminContextPath;

		public SecuritySecureConfig(AdminServerProperties adminServerProperties) {
			this.adminContextPath = adminServerProperties.getContextPath();
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
			successHandler.setTargetUrlParameter("redirectTo");
			successHandler.setDefaultTargetUrl(adminContextPath + "/");

			http.authorizeRequests()
					.antMatchers(adminContextPath + "/assets/**").permitAll() // <1>
					.antMatchers(adminContextPath + "/login").permitAll()
					.anyRequest().authenticated() // <2>
					.and()
					.formLogin().loginPage(adminContextPath + "/login").successHandler(successHandler).and() // <3>
					.logout().logoutUrl(adminContextPath + "/logout").and()
					.httpBasic().and() // <4>
					.csrf()
					.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())  // <5>
					.ignoringAntMatchers(
							adminContextPath + "/instances",   // <6>
							adminContextPath + "/actuator/**"  // <7>
					);
			// @formatter:on
		}
	}
	// end::configuration-spring-security[]

	// tag::customization-instance-exchange-filter-function[]
	@Bean
	public InstanceExchangeFilterFunction auditLog() {
		return (instance, request, next) -> {
			if (HttpMethod.DELETE.equals(request.method()) || HttpMethod.POST.equals(request.method())) {
				log.info("{} for {} on {}", request.method(), instance.getId(), request.url());
			}
			return next.exchange(request);
		};
	}
	// end::customization-instance-exchange-filter-function[]

	@Bean
	public CustomNotifier customNotifier(InstanceRepository repository) {
		return new CustomNotifier(repository);
	}

	@Bean
	public CustomEndpoint customEndpoint() {
		return new CustomEndpoint();
	}

	// tag::customization-http-headers-providers[]
	@Bean
	public HttpHeadersProvider customHttpHeadersProvider() {
		return  instance -> {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("X-CUSTOM", "My Custom Value");
			return httpHeaders;
		};
	}
	// end::customization-http-headers-providers[]


	// tag::configuration-filtering-notifier[]
	@Configuration
	public static class NotifierConfig {
		private final InstanceRepository repository;
		private final ObjectProvider<List<Notifier>> otherNotifiers;

		public NotifierConfig(InstanceRepository repository, ObjectProvider<List<Notifier>> otherNotifiers) {
			this.repository = repository;
			this.otherNotifiers = otherNotifiers;
		}

		@Bean
		public FilteringNotifier filteringNotifier() { // <1>
			CompositeNotifier delegate = new CompositeNotifier(otherNotifiers.getIfAvailable(Collections::emptyList));
			return new FilteringNotifier(delegate, repository);
		}

		@Primary
		@Bean(initMethod = "start", destroyMethod = "stop")
		public RemindingNotifier remindingNotifier() { // <2>
			RemindingNotifier notifier = new RemindingNotifier(filteringNotifier(), repository);
			notifier.setReminderPeriod(Duration.ofMinutes(10));
			notifier.setCheckReminderInverval(Duration.ofSeconds(10));
			return notifier;
		}
	}
	// end::configuration-filtering-notifier[]
}
@Endpoint(id = "custom")
class CustomEndpoint {
	@ReadOperation
	public String invoke() {
		return "Hello World!";
	}
}

@Slf4j
class CustomNotifier extends AbstractEventNotifier {

	public CustomNotifier(InstanceRepository repository) {
		super(repository);
	}
	@Override
	protected Mono<Void> doNotify(InstanceEvent instanceEvent, Instance instance) {
		return Mono.fromRunnable(() -> {
			if (instanceEvent instanceof InstanceStatusChangedEvent) {
				log.info("Instance {} ({}) is {}", instance.getRegistration().getName(), instanceEvent.getInstance(),
						((InstanceStatusChangedEvent) instanceEvent).getStatusInfo().getStatus());
			} else {
				log.info("Instance {} ({}) {}", instance.getRegistration().getName(), instanceEvent.getInstance(),
						instanceEvent.getType());
			}
		});
	}
}