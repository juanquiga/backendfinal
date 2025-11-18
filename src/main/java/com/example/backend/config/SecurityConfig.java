package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFrameOptionsMode;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .csrf().disable() // si usas JWT o APIs, normalmente se desactiva
          .authorizeHttpRequests(auth -> auth
            .requestMatchers("/h2-console/**", "/api/auth/**", "/login.html", "/registro.html", "/", "/index.html", "/static/**", "/**.js", "/**.css").permitAll()
            .anyRequest().authenticated()
          )
          .headers(headers -> headers
            .frameOptions(frame -> frame.disable()) // permitir H2 console
          );

        return http.build();
    }
}