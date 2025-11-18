package com.example.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.model.Pedido;
import com.example.backend.repository.PedidoRepository;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoRepository repo;

    public PedidoController(PedidoRepository repo) {
        this.repo = repo;
    }

    // Crear pedido (público)
    @PostMapping
    public ResponseEntity<Pedido> crear(@RequestBody Pedido p) {
        p.setEstado("PENDIENTE");
        return ResponseEntity.ok(repo.save(p));
    }

    // Listar pedidos (protegido por default)
    @GetMapping
    public List<Pedido> all() {
        return repo.findAll();
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody Map<String,String> body) {
        return repo.findById(id).map(p -> {
            p.setEstado(body.getOrDefault("estado", p.getEstado()));
            repo.save(p);
            return ResponseEntity.ok(p);
        }).orElse(ResponseEntity.notFound().build());
    }
}