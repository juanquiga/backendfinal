package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.example.backend.security.JwtAuthFilter;
import com.example.backend.security.JwtUtils;
import com.example.backend.repository.UsuarioRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final UsuarioRepository usuarioRepo;

    public SecurityConfig(JwtUtils jwtUtils, UsuarioRepository usuarioRepo) {
        this.jwtUtils = jwtUtils;
        this.usuarioRepo = usuarioRepo;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .csrf().disable()
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/api/auth/**", "/", "/index.html", "/static/**", "/swagger-ui/**").permitAll()
              .anyRequest().authenticated()
          );

        // Si tienes JwtAuthFilter compilado y funcionando, lo añadimos antes del filtro de autenticación por defecto:
        try {
          JwtAuthFilter jwtFilter = new JwtAuthFilter(jwtUtils, usuarioRepo);
          http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        } catch (Exception e) {
          // Si no quieres usar el filtro ahora, coméntalo o elimina este bloque.
        }

        return http.build();
    }
}