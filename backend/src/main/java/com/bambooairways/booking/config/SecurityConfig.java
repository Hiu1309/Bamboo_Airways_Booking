package com.bambooairways.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain applicationSecurity(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                // Authentication for user routes is handled by the application's JWT service.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/payments/webhook/**").permitAll()
                        .anyRequest().permitAll())
                .build();
    }
}
