package com.example.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.backend.model.Usuario;
import com.example.backend.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepo, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!usuarioRepo.existsByUsername("admin")) {
            Usuario admin = new Usuario("admin", passwordEncoder.encode("admin123"), "ROLE_ADMIN");
            usuarioRepo.save(admin);
            System.out.println("Admin creado -> admin / admin123");
        }
        if (!usuarioRepo.existsByUsername("cliente")) {
            Usuario cliente = new Usuario("cliente", passwordEncoder.encode("cliente123"), "ROLE_USER");
            usuarioRepo.save(cliente);
            System.out.println("Cliente creado -> cliente / cliente123");
        }
    }
}