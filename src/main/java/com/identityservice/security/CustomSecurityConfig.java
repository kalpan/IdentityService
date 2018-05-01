package com.identityservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class CustomSecurityConfig extends WebSecurityConfigurerAdapter {
	
	/** A realm is a credential store that enables identity or role based access control. **/
	private static String REALM = "IDENTITY_DEMO_REALM";
	
	/** Core interface which loads user-specific data. */
	@Autowired
    private UserDetailsService userDetailsService;

	/** InMemoryAuthentication needs this. */
	@SuppressWarnings("deprecation")
	@Bean
	public static NoOpPasswordEncoder passwordEncoder() {
		return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
	}

	/** Let's get started with two users and their roles: admin and a generic user. We'll add more later via the REST API. */
	@Autowired
    protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
		
        auth.inMemoryAuthentication()
           .withUser("admin").password("admin").roles("USER", "ADMIN")
           .and()
           .withUser("john").password("doe").roles("USER");
   }
	
	/** This is where we assign Role based access to endpoints. It'possible to add other types of authentication here.*/
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		/** I don't think we need CSRF protection as we're enforcing authentication.*/
	  http.csrf().disable()
	  	.authorizeRequests()
	  	.antMatchers("/api/admin/user/**").hasRole("ADMIN")
	  	.antMatchers("/api/user/**").hasRole("USER")
	  	.anyRequest().authenticated()
		.and().httpBasic().realmName(REALM).authenticationEntryPoint(getBasicAuthEntryPoint());
 	}
	
	/** Basic Authentication entry point is defined. */
	@Bean
	public IndentityBasicAuthenticationEntryPoint getBasicAuthEntryPoint(){
		return new IndentityBasicAuthenticationEntryPoint();
	}

}
