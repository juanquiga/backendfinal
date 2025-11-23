package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.model.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Obtiene todos los pedidos de forma optimizada.
     * 
     * @return Lista de pedidos
     */
    @Query("SELECT p FROM Pedido p ORDER BY p.id DESC")
    List<Pedido> obtenerTodos();

    /**
     * Obtiene un pedido por ID de forma optimizada.
     * 
     * @param id ID del pedido
     * @return Optional con el pedido
     */
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> obtenerPorId(@Param("id") Long id);

    /**
     * Obtiene todos los pedidos PENDIENTES.
     * 
     * OPTIMIZACIÓN:
     * - Filtrado en BD en lugar de en memoria (stream.filter)
     * - Si hay índice en 'estado', es muy rápido
     * - Mejor para dashboards en tiempo real
     * 
     * SQL:
     * SELECT p FROM pedidos p WHERE p.estado = 'PENDIENTE' ORDER BY p.id DESC
     * 
     * @return Lista de pedidos pendientes
     */
    @Query("SELECT p FROM Pedido p WHERE p.estado = 'PENDIENTE' ORDER BY p.id DESC")
    List<Pedido> obtenerPendientes();

    /**
     * Obtiene todos los pedidos ATENDIDOS.
     * 
     * @return Lista de pedidos atendidos
     */
    @Query("SELECT p FROM Pedido p WHERE p.estado = 'ATENDIDO' ORDER BY p.id DESC")
    List<Pedido> obtenerAtendidos();

    /**
     * Obtiene todos los pedidos CANCELADOS.
     * 
     * @return Lista de pedidos cancelados
     */
    @Query("SELECT p FROM Pedido p WHERE p.estado = 'CANCELADO' ORDER BY p.id DESC")
    List<Pedido> obtenerCancelados();

    /**
     * Obtiene pedidos por estado genérico (parametrizado).
     * 
     * OPTIMIZACIÓN:
     * - Parámetro vinculado previene SQL injection
     * - Consulta reusable para cualquier estado
     * - Prepared statement cacheado por BD
     * 
     * @param estado Estado a filtrar
     * @return Lista de pedidos
     */
    @Query("SELECT p FROM Pedido p WHERE p.estado = :estado ORDER BY p.id DESC")
    List<Pedido> obtenerPorEstado(@Param("estado") String estado);

    /**
     * Busca pedidos por nombre de cliente (búsqueda parcial).
     * 
     * OPTIMIZACIÓN:
     * - LIKE en BD
     * - Case-insensitive
     * 
     * @param nombreCliente Nombre a buscar
     * @return Pedidos coincidentes
     */
    @Query("SELECT p FROM Pedido p WHERE LOWER(p.nombreCliente) LIKE LOWER(CONCAT('%', :nombre, '%')) ORDER BY p.id DESC")
    List<Pedido> buscarPorCliente(@Param("nombre") String nombreCliente);

    /**
     * Obtiene pedidos en rango de total (para filtros por precio).
     * 
     * @param minTotal Monto mínimo
     * @param maxTotal Monto máximo
     * @return Pedidos en rango
     */
    @Query("SELECT p FROM Pedido p WHERE p.total >= :minTotal AND p.total <= :maxTotal ORDER BY p.total DESC")
    List<Pedido> obtenerPorRangoTotal(@Param("minTotal") Integer minTotal, @Param("maxTotal") Integer maxTotal);

    /**
     * Obtiene pedidos más recientes (para dashboard).
     * 
     * OPTIMIZACIÓN:
     * - LIMIT en BD, no en aplicación
     * - Muy eficiente para últimos N registros
     * 
     * @param limite Cantidad máxima
     * @return Últimos N pedidos
     */
    @Query("SELECT p FROM Pedido p ORDER BY p.id DESC LIMIT :limite")
    List<Pedido> obtenerUltimos(@Param("limite") int limite);

    /**
     * Cuenta pedidos por estado (para estadísticas).
     * 
     * OPTIMIZACIÓN:
     * - COUNT en BD es muy rápido
     * - No carga datos completos
     * 
     * @param estado Estado a contar
     * @return Cantidad de pedidos
     */
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.estado = :estado")
    long contar(@Param("estado") String estado);

    /**
     * Obtiene suma total de ventas por estado.
     * 
     * OPTIMIZACIÓN:
     * - SUM en BD (agregación)
     * - Ideal para reportes financieros
     * 
     * @param estado Estado a sumar
     * @return Total en COP
     */
    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.estado = :estado")
    Long obtenerTotalPorEstado(@Param("estado") String estado);

    /**
     * Obtiene promedio de total por estado.
     * 
     * @param estado Estado
     * @return Promedio en COP
     */
    @Query("SELECT COALESCE(AVG(p.total), 0) FROM Pedido p WHERE p.estado = :estado")
    Double obtenerPromedioPorEstado(@Param("estado") String estado);

    /**
     * Actualiza estado de un pedido (optimizado - una sola consulta).
     * 
     * OPTIMIZACIÓN:
     * - Una consulta SQL: UPDATE en lugar de SELECT + UPDATE
     * - No carga todo el objeto
     * - Transaccional automático
     * 
     * SQL:
     * UPDATE pedidos SET estado = :nuevoEstado WHERE id = :id
     * 
     * @param id ID del pedido
     * @param nuevoEstado Nuevo estado
     * @return Filas actualizadas (0 o 1)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Pedido p SET p.estado = :nuevoEstado WHERE p.id = :id")
    int actualizarEstado(@Param("id") Long id, @Param("nuevoEstado") String nuevoEstado);

    /**
     * Actualiza estado de múltiples pedidos (batch update).
     * 
     * OPTIMIZACIÓN:
     * - Una consulta para múltiples registros
     * - Muy eficiente para operaciones masivas
     * 
     * @param estadoAnterior Estado actual
     * @param estadoNuevo Estado nuevo
     * @return Cantidad de filas actualizadas
     */
    @Modifying
    @Transactional
    @Query("UPDATE Pedido p SET p.estado = :estadoNuevo WHERE p.estado = :estadoAnterior")
    int actualizarEstadoMasivo(@Param("estadoAnterior") String estadoAnterior, @Param("estadoNuevo") String estadoNuevo);

    /**
     * Verifica existencia sin cargar objeto (usa EXISTS).
     * 
     * OPTIMIZACIÓN:
     * - EXISTS es muy rápido en BD
     * - No carga datos innecesarios
     * 
     * @param id ID a verificar
     * @return true si existe
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Pedido p WHERE p.id = :id")
    boolean existePorId(@Param("id") Long id);

    /**
     * Obtiene IDs de pedidos pendientes (para procesamiento batch).
     * 
     * OPTIMIZACIÓN:
     * - Carga solo IDs (muy ligero)
     * - Útil para procesamiento en lotes
     * 
     * @return Lista de IDs
     */
    @Query("SELECT p.id FROM Pedido p WHERE p.estado = 'PENDIENTE'")
    List<Long> obtenerIdsPendientes();

    /**
     * Obtiene estadísticas generales de pedidos.
     * 
     * OPTIMIZACIÓN:
     * - Todas las agregaciones en una sola consulta
     * - Ideal para dashboards
     * 
     * @return Object con estadísticas
     */
    @Query("SELECT " +
           "COUNT(p) as total, " +
           "SUM(CASE WHEN p.estado = 'PENDIENTE' THEN 1 ELSE 0 END) as pendientes, " +
           "SUM(CASE WHEN p.estado = 'ATENDIDO' THEN 1 ELSE 0 END) as atendidos, " +
           "SUM(CASE WHEN p.estado = 'CANCELADO' THEN 1 ELSE 0 END) as cancelados, " +
           "SUM(p.total) as totalVentas, " +
           "AVG(p.total) as promedioVentas " +
           "FROM Pedido p")
    Object obtenerEstadisticas();
}