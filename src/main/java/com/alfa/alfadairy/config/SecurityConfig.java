package com.alfa.alfadairy.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http.authorizeHttpRequests(auth -> auth.requestMatchers(
                PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .mvcMatchers(
                        "/",
                        "/sign-up"
                ).permitAll()
                .anyRequest().authenticated()).build();
    }
}
