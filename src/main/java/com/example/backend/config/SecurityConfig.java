package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.backend.repository.UsuarioRepository;
import com.example.backend.security.JwtAuthFilter;
import com.example.backend.security.JwtUtils;

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
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthFilter jwtFilter = new JwtAuthFilter(jwtUtils, usuarioRepo);

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/productos/**", "/h2-console/**", "/").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        // H2 console frame
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}