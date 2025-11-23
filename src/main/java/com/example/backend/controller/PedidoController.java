package com.example.backend.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.PedidoDTO;
import com.example.backend.dto.ResponseDTO;
import com.example.backend.service.PedidoService;

/**
 * Controlador REST para gestión de Pedidos.
 * 
 * Endpoints:
 * GET    /api/pedidos              - Obtiene todos los pedidos
 * GET    /api/pedidos/{id}         - Obtiene un pedido por ID
 * GET    /api/pedidos/estado/{est} - Obtiene pedidos por estado
 * POST   /api/pedidos              - Crea un nuevo pedido
 * PUT    /api/pedidos/{id}/estado  - Actualiza el estado de un pedido
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * POST /api/pedidos
     * Crea un nuevo pedido en el sistema.
     * El pedido se inicia con estado "PENDIENTE".
     * 
     * @param pedidoDTO Datos del nuevo pedido (validados)
     * @return ResponseEntity con el pedido creado (201)
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<PedidoDTO>> crear(@Valid @RequestBody PedidoDTO pedidoDTO) {
        PedidoDTO pedidoCreado = pedidoService.crear(pedidoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ResponseDTO.success("Pedido creado exitosamente", pedidoCreado)
        );
    }

    /**
     * GET /api/pedidos
     * Obtiene la lista de todos los pedidos.
     * 
     * @return ResponseEntity con lista de pedidos y código 200
     */
    @GetMapping
    public ResponseEntity<ResponseDTO<List<PedidoDTO>>> obtenerTodos() {
        List<PedidoDTO> pedidos = pedidoService.obtenerTodos();
        return ResponseEntity.ok(
            ResponseDTO.success("Pedidos obtenidos exitosamente", pedidos)
        );
    }

    /**
     * GET /api/pedidos/{id}
     * Obtiene un pedido específico por su ID.
     * 
     * @param id ID del pedido a obtener
     * @return ResponseEntity con el pedido encontrado (200) o error (404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<PedidoDTO>> obtenerPorId(@PathVariable Long id) {
        PedidoDTO pedido = pedidoService.obtenerPorId(id);
        return ResponseEntity.ok(
            ResponseDTO.success("Pedido obtenido exitosamente", pedido)
        );
    }

    /**
     * GET /api/pedidos/estado/{estado}
     * Obtiene todos los pedidos con un estado específico.
     * Estados válidos: PENDIENTE, ATENDIDO, CANCELADO
     * 
     * @param estado Estado a filtrar
     * @return ResponseEntity con lista de pedidos del estado especificado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<ResponseDTO<List<PedidoDTO>>> obtenerPorEstado(@PathVariable String estado) {
        List<PedidoDTO> pedidos = pedidoService.obtenerPorEstado(estado.toUpperCase());
        return ResponseEntity.ok(
            ResponseDTO.success("Pedidos obtenidos por estado: " + estado, pedidos)
        );
    }

    /**
     * PUT /api/pedidos/{id}/estado
     * Actualiza el estado de un pedido existente.
     * Estados válidos: PENDIENTE, ATENDIDO, CANCELADO
     * 
     * Ejemplo de request body:
     * {
     *   "estado": "ATENDIDO"
     * }
     * 
     * @param id ID del pedido
     * @param estado Nuevo estado del pedido
     * @return ResponseEntity con el pedido actualizado (200)
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<ResponseDTO<PedidoDTO>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        PedidoDTO pedidoActualizado = pedidoService.cambiarEstado(id, estado.toUpperCase());
        return ResponseEntity.ok(
            ResponseDTO.success("Estado del pedido actualizado exitosamente", pedidoActualizado)
        );
    }
}