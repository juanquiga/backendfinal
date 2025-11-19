package com.example.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.backend.model.Usuario;
import com.example.backend.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(UsuarioRepository repo, BCryptPasswordEncoder encoder) {
        return args -> {
            if (!repo.existsByUsername("admin")) {
                Usuario admin = new Usuario("admin", encoder.encode("admin123"), "ROLE_ADMIN");
                repo.save(admin);
            }
            if (!repo.existsByUsername("cliente")) {
                Usuario c = new Usuario("cliente", encoder.encode("cliente123"), "ROLE_USER");
                repo.save(c);
            }
        };
    }
}