// src/main/java/com/example/backend/config/WebConfig.java
package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
      .allowedOrigins(
         "https://TU_USUARIO.github.io",     // reemplaza por tu GitHub Pages si lo usas
         "http://localhost:5500",            // opcional: para pruebas locales
         "https://backendfinal-rkrx.onrender.com" // opcional si frontend alguna vez se sirve desde otro dominio
      )
      .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
      .allowCredentials(true)
      .maxAge(3600);
  }
}