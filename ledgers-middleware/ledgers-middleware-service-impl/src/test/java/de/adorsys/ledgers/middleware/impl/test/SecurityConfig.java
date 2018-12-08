package de.adorsys.ledgers.middleware.impl.test;

import java.security.Principal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;

@Configuration
public class SecurityConfig {

	@Bean
	public Principal getPrincipal(){
		return () -> "anonymous";
	}
	
	@Bean AccessTokenTO getAccessTokenTO() {
		AccessTokenTO token = new AccessTokenTO();
		token.setActor("anonymous");
		token.setSub("anonymous");
		return token;
	}
}
