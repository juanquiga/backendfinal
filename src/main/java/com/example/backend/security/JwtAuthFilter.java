package com.example.backend.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.backend.model.Usuario;
import com.example.backend.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UsuarioRepository usuarioRepo;

    public JwtAuthFilter(JwtUtils jwtUtils, UsuarioRepository usuarioRepo) {
        this.jwtUtils = jwtUtils;
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtils.validateJwtToken(token)) {
                String username = jwtUtils.getUsernameFromToken(token);
                Optional<Usuario> optUser = usuarioRepo.findByUsername(username);
                if (optUser.isPresent()) {
                    Usuario user = optUser.get();
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null,
                            List.of(new SimpleGrantedAuthority(user.getRole()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}