package com.example.backend.service;

import org.springframework.stereotype.Service;
import com.example.backend.model.Pedido;
import com.example.backend.dto.PedidoDTO;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.PedidoRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Pedidos.
 * Gestiona la lógica de negocio relacionada con pedidos.
 * Responsabilidades:
 * - Validación de pedidos
 * - Gestión de estado de pedidos
 * - Transformación de DTOs
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    // Estados válidos de pedidos
    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_ATENDIDO = "ATENDIDO";
    private static final String ESTADO_CANCELADO = "CANCELADO";

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Obtiene todos los pedidos.
     * @return Lista de PedidoDTO
     */
    public List<PedidoDTO> obtenerTodos() {
        return pedidoRepository.obtenerTodos()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un pedido por ID.
     * @param id ID del pedido
     * @return PedidoDTO
     * @throws ResourceNotFoundException si el pedido no existe
     */
    public PedidoDTO obtenerPorId(Long id) {
        Pedido pedido = pedidoRepository.obtenerPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
        return convertirADTO(pedido);
    }

    /**
     * Crea un nuevo pedido.
     * Validaciones:
     * - El total debe ser positivo
     * - Debe tener al menos un item
     * 
     * @param pedidoDTO DTO con los datos del pedido
     * @return PedidoDTO del pedido creado
     * @throws BusinessException si los datos no son válidos
     */
    public PedidoDTO crear(PedidoDTO pedidoDTO) {
        // Validar total
        if (pedidoDTO.getTotal() == null || pedidoDTO.getTotal() <= 0) {
            throw new BusinessException("El total debe ser un valor positivo");
        }

        // Validar items
        if (pedidoDTO.getItemsJson() == null || pedidoDTO.getItemsJson().trim().isEmpty()) {
            throw new BusinessException("El pedido debe contener al menos un item");
        }

        Pedido pedido = convertirDesdeDTO(pedidoDTO);
        pedido.setEstado(ESTADO_PENDIENTE); // Estado inicial
        
        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        return convertirADTO(pedidoGuardado);
    }

    /**
     * Actualiza el estado de un pedido.
     * Estados válidos: PENDIENTE, ATENDIDO, CANCELADO
     * 
     * @param id ID del pedido
     * @param nuevoEstado Nuevo estado del pedido
     * @return PedidoDTO del pedido actualizado
     * @throws ResourceNotFoundException si el pedido no existe
     * @throws BusinessException si el estado no es válido
     */
    public PedidoDTO cambiarEstado(Long id, String nuevoEstado) {
        // Validar estado
        if (!esEstadoValido(nuevoEstado)) {
            throw new BusinessException(
                    "Estado inválido. Estados válidos: " + ESTADO_PENDIENTE + ", " + 
                    ESTADO_ATENDIDO + ", " + ESTADO_CANCELADO
            );
        }

        // Verificar que existe
        if (!pedidoRepository.existePorId(id)) {
            throw new ResourceNotFoundException("Pedido", "id", id);
        }

        // Actualizar directamente en BD (mucho más rápido)
        pedidoRepository.actualizarEstado(id, nuevoEstado);
        
        // Obtener el pedido actualizado para retornar
        Pedido pedidoActualizado = pedidoRepository.obtenerPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
        return convertirADTO(pedidoActualizado);
    }

    /**
     * Obtiene todos los pedidos pendientes.
     * Útil para mostrar los pedidos en espera de atención.
     * 
     * @return Lista de PedidoDTO pendientes
     */
    public List<PedidoDTO> obtenerPendientes() {
        return pedidoRepository.obtenerPendientes()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene pedidos por estado.
     * @param estado Estado a filtrar
     * @return Lista de PedidoDTO con el estado especificado
     */
    public List<PedidoDTO> obtenerPorEstado(String estado) {
        if (!esEstadoValido(estado)) {
            throw new BusinessException("Estado inválido: " + estado);
        }
        
        return pedidoRepository.obtenerPorEstado(estado)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca pedidos por nombre de cliente.
     * 
     * NUEVA FUNCIONALIDAD:
     * - Búsqueda parcial case-insensitive
     * - Filtrado en BD
     * 
     * @param nombreCliente Nombre a buscar
     * @return Pedidos del cliente
     */
    public List<PedidoDTO> buscarPorCliente(String nombreCliente) {
        return pedidoRepository.buscarPorCliente(nombreCliente)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de pedidos.
     * 
     * 
     * @return Object con estadísticas
     */
    public Object obtenerEstadisticas() {
        return pedidoRepository.obtenerEstadisticas();
    }

    /**
     * Valida si un estado es válido.
     */
    private boolean esEstadoValido(String estado) {
        return ESTADO_PENDIENTE.equals(estado) || 
               ESTADO_ATENDIDO.equals(estado) || 
               ESTADO_CANCELADO.equals(estado);
    }

    /**
     * Convierte una entidad Pedido a PedidoDTO.
     */
    private PedidoDTO convertirADTO(Pedido pedido) {
        return new PedidoDTO(
                pedido.getId(),
                pedido.getNombreCliente(),
                pedido.getTelefono(),
                pedido.getDireccion(),
                pedido.getTotal(),
                pedido.getItemsJson(),
                pedido.getEstado()
        );
    }

    /**
     * Convierte un PedidoDTO a entidad Pedido.
     */
    private Pedido convertirDesdeDTO(PedidoDTO dto) {
        Pedido pedido = new Pedido();
        pedido.setNombreCliente(dto.getNombreCliente());
        pedido.setTelefono(dto.getTelefono());
        pedido.setDireccion(dto.getDireccion());
        pedido.setTotal(dto.getTotal());
        pedido.setItemsJson(dto.getItemsJson());
        return pedido;
    }
}
