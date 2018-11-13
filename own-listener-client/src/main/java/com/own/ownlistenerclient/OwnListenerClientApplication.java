package com.own.ownlistenerclient;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableAutoConfiguration
@EnableAdminServer
public class OwnListenerClientApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application;
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
					.antMatchers(adminContextPath + "/assets/**").permitAll()
					.antMatchers(adminContextPath + "/login").permitAll()
					.anyRequest().authenticated()
					.and()
					.formLogin().loginPage(adminContextPath + "/login").successHandler(successHandler).and()
					.logout().logoutUrl(adminContextPath + "/logout").and()
					.httpBasic().and()
					.csrf()
					.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
					.ignoringAntMatchers(adminContextPath + "/instances", adminContextPath + "/actuator/**");
			// @formatter:on
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(OwnListenerClientApplication.class, args);
	}
}
