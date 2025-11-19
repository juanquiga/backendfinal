package com.example.backend.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.model.Usuario;
import com.example.backend.repository.UsuarioRepository;
import com.example.backend.security.JwtUtils;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(UsuarioRepository usuarioRepo, BCryptPasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.usuarioRepo = usuarioRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String,String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
        }

        if (usuarioRepo.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "username exists"));
        }

        Usuario u = new Usuario(username, passwordEncoder.encode(password), "ROLE_USER");
        usuarioRepo.save(u);

        return ResponseEntity.ok(Map.of("msg", "registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<Usuario> opt = usuarioRepo.findByUsername(username);
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid credentials"));
        }

        Usuario u = opt.get();

        if (!passwordEncoder.matches(password, u.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid credentials"));
        }

        String token = jwtUtils.generateToken(u.getUsername());

        return ResponseEntity.ok(Map.of("token", token));
    }
}