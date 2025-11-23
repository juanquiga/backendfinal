package com.example.backend.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ProductoDTO;
import com.example.backend.dto.ResponseDTO;
import com.example.backend.service.ProductoService;

/**
 * Controlador REST para gestión de Productos.
 * 
 * Endpoints:
 * GET    /api/productos           - Obtiene todos los productos
 * GET    /api/productos/{id}      - Obtiene un producto por ID
 * POST   /api/productos           - Crea un nuevo producto
 * PUT    /api/productos/{id}      - Actualiza un producto existente
 * DELETE /api/productos/{id}      - Elimina un producto
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * GET /api/productos
     * Obtiene la lista de todos los productos disponibles.
     * 
     * @return ResponseEntity con lista de productos y código 200
     */
    @GetMapping
    public ResponseEntity<ResponseDTO<List<ProductoDTO>>> obtenerTodos() {
        List<ProductoDTO> productos = productoService.obtenerTodos();
        return ResponseEntity.ok(
            ResponseDTO.success("Productos obtenidos exitosamente", productos)
        );
    }

    /**
     * GET /api/productos/{id}
     * Obtiene un producto específico por su ID.
     * 
     * @param id ID del producto a obtener
     * @return ResponseEntity con el producto encontrado (200) o error (404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<ProductoDTO>> obtenerPorId(@PathVariable Long id) {
        ProductoDTO producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(
            ResponseDTO.success("Producto obtenido exitosamente", producto)
        );
    }

    /**
     * POST /api/productos
     * Crea un nuevo producto en el sistema.
     * 
     * @param productoDTO Datos del nuevo producto (validados)
     * @return ResponseEntity con el producto creado (201)
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<ProductoDTO>> crear(@Valid @RequestBody ProductoDTO productoDTO) {
        ProductoDTO productoCreado = productoService.crear(productoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ResponseDTO.success("Producto creado exitosamente", productoCreado)
        );
    }

    /**
     * PUT /api/productos/{id}
     * Actualiza un producto existente.
     * 
     * @param id ID del producto a actualizar
     * @param productoDTO Nuevos datos del producto
     * @return ResponseEntity con el producto actualizado (200)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<ProductoDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoDTO productoDTO) {
        ProductoDTO productoActualizado = productoService.actualizar(id, productoDTO);
        return ResponseEntity.ok(
            ResponseDTO.success("Producto actualizado exitosamente", productoActualizado)
        );
    }

    /**
     * DELETE /api/productos/{id}
     * Elimina un producto del sistema.
     * 
     * @param id ID del producto a eliminar
     * @return ResponseEntity con código 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<?>> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}