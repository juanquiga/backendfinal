package com.example.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.backend.dto.ProductoDTO;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Producto;
import com.example.backend.repository.ProductoRepository;

/**
 * Servicio de Productos.
 * Contiene la lógica de negocio separada del controlador.
 * Responsabilidades:
 * - Validación de reglas de negocio
 * - Transformación entre DTO y entidades
 * - Operaciones con la base de datos a través del repositorio
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final com.example.backend.repository.UsuarioRepository usuarioRepository;
    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;
    private final com.example.backend.security.JwtUtils jwtUtils;

    public ProductoService(ProductoRepository productoRepository,
                          com.example.backend.repository.UsuarioRepository usuarioRepository,
                          org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder,
                          com.example.backend.security.JwtUtils jwtUtils) {
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public com.example.backend.repository.UsuarioRepository getUsuarioRepository() {
        return usuarioRepository;
    }

    public org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public com.example.backend.security.JwtUtils getJwtUtils() {
        return jwtUtils;
    }

    /**
     * Obtiene todos los productos.
     * @return Lista de ProductoDTO
     */
    public List<ProductoDTO> obtenerTodos() {
        return productoRepository.obtenerTodos()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un producto por ID.
     * @param id ID del producto
     * @return ProductoDTO
     * @throws ResourceNotFoundException si el producto no existe
     */
    public ProductoDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.obtenerPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
        return convertirADTO(producto);
    }

    /**
     * Crea un nuevo producto.
     * @param productoDTO DTO con los datos del producto
     * @return ProductoDTO del producto creado
     * @throws BusinessException si los datos no son válidos
     */
    public ProductoDTO crear(ProductoDTO productoDTO) {
        // Validar que el precio sea válido
        if (productoDTO.getPrecio() == null || productoDTO.getPrecio() < 0) {
            throw new BusinessException("El precio debe ser un valor positivo");
        }

        Producto producto = convertirDesdeDTO(productoDTO);
        Producto productoGuardado = productoRepository.save(producto);
        return convertirADTO(productoGuardado);
    }

    /**
     * Actualiza un producto existente.
     * @param id ID del producto a actualizar
     * @param productoDTO DTO con los nuevos datos
     * @return ProductoDTO del producto actualizado
     * @throws ResourceNotFoundException si el producto no existe
     */
    public ProductoDTO actualizar(Long id, ProductoDTO productoDTO) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        // Validar datos
        if (productoDTO.getPrecio() != null && productoDTO.getPrecio() < 0) {
            throw new BusinessException("El precio debe ser un valor positivo");
        }

        // Actualizar campos
        if (productoDTO.getNombre() != null) {
            productoExistente.setNombre(productoDTO.getNombre());
        }
        if (productoDTO.getDescripcion() != null) {
            productoExistente.setDescripcion(productoDTO.getDescripcion());
        }
        if (productoDTO.getPrecio() != null) {
            productoExistente.setPrecio(productoDTO.getPrecio());
        }
        if (productoDTO.getImagenUrl() != null) {
            productoExistente.setImagenUrl(productoDTO.getImagenUrl());
        }

        Producto productoActualizado = productoRepository.save(productoExistente);
        return convertirADTO(productoActualizado);
    }

    /**
     * Elimina un producto.
     * @param id ID del producto a eliminar
     * @throws ResourceNotFoundException si el producto no existe
     */
    public void eliminar(Long id) {
        if (!productoRepository.existePorId(id)) {
            throw new ResourceNotFoundException("Producto", "id", id);
        }
        productoRepository.deleteById(id);
    }

    /**
     * Busca productos por nombre.
     * 
     * NUEVA FUNCIONALIDAD + OPTIMIZACIÓN:
     * - Búsqueda parcial en BD (case-insensitive)
     * - Filtrado en BD, no en memoria
     * 
     * @param nombre Nombre a buscar
     * @return Lista de productos coincidentes
     */
    public List<ProductoDTO> buscarPorNombre(String nombre) {
        return productoRepository.buscarPorNombre(nombre)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene productos en rango de precio.
     * 
     * NUEVA FUNCIONALIDAD + OPTIMIZACIÓN:
     * - Filtrado por rango en BD
     * - Ordenado por precio
     * 
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Productos en el rango
     */
    public List<ProductoDTO> obtenerPorRangoPrecio(Integer precioMin, Integer precioMax) {
        return productoRepository.obtenerPorRangoPrecio(precioMin, precioMax)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad Producto a ProductoDTO.
     */
    private ProductoDTO convertirADTO(Producto producto) {
        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getImagenUrl()
        );
    }

    /**
     * Convierte un ProductoDTO a entidad Producto.
     */
    private Producto convertirDesdeDTO(ProductoDTO dto) {
        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setImagenUrl(dto.getImagenUrl());
        return producto;
    }
}
