package com.example.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.backend.model.Producto;
import com.example.backend.model.Usuario;
import com.example.backend.repository.ProductoRepository;
import com.example.backend.repository.UsuarioRepository;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner init(ProductoRepository productoRepo, UsuarioRepository usuarioRepo, BCryptPasswordEncoder encoder) {
        return args -> {
            if (productoRepo.count() == 0) {
                Producto p1 = new Producto(); p1.setNombre("Galleta Choco"); p1.setDescripcion("Deliciosa"); p1.setPrecio(2500); p1.setImagenUrl("/img/galleta1.jpg");
                Producto p2 = new Producto(); p2.setNombre("Galleta Vainilla"); p2.setDescripcion("Suave"); p2.setPrecio(2300); p2.setImagenUrl("/img/galleta2.jpg");
                productoRepo.save(p1); productoRepo.save(p2);
            }
            if (usuarioRepo.count() == 0) {
                usuarioRepo.save(new Usuario("admin", encoder.encode("admin123"), "ROLE_ADMIN"));
                usuarioRepo.save(new Usuario("user", encoder.encode("user123"), "ROLE_USER"));
            }
        };
    }
}