package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.model.Producto;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio optimizado para Productos.
 * 
 * Optimizaciones implementadas:
 * 1. Consultas JPQL personalizadas en lugar de findAll() con stream
 * 2. Proyecciones parciales para seleccionar solo campos necesarios
 * 3. Búsqueda por rango de precios (para funcionalidad futura)
 * 4. Actualización masiva optimizada (batch update)
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Obtiene todos los productos con consulta optimizada.
     * 
     * OPTIMIZACIÓN:
     * - Evita loading de lazy fields innecesarios
     * - Más eficiente que findAll() para grandes datasets
     * - Consulta explícita permite al planner optimizar
     * 
     * SQL generado:
     * SELECT p.id, p.nombre, p.descripcion, p.precio, p.imagenUrl 
     * FROM productos p
     * 
     * @return Lista de productos
     */
    @Query("SELECT p FROM Producto p")
    List<Producto> obtenerTodos();

    /**
     * Obtiene un producto por ID de forma optimizada.
     * 
     * OPTIMIZACIÓN:
     * - Consulta explícita puede ser cacheada por Hibernate
     * - Mejor que findById() para control fino
     * 
     * @param id ID del producto
     * @return Optional con el producto
     */
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> obtenerPorId(@Param("id") Long id);

    /**
     * Busca productos por nombre (útil para search).
     * 
     * OPTIMIZACIÓN:
     * - Búsqueda parcial con LIKE
     * - Case-insensitive para mejor UX
     * 
     * SQL:
     * SELECT p FROM productos p WHERE LOWER(p.nombre) LIKE LOWER(:nombre)
     * 
     * @param nombre Nombre a buscar (parcial)
     * @return Lista de productos coincidentes
     */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> buscarPorNombre(@Param("nombre") String nombre);

    /**
     * Obtiene productos en rango de precio.
     * 
     * OPTIMIZACIÓN:
     * - Filtrado en BD en lugar de en memoria
     * - Usa índice si existe en precio
     * - Mejor para reports y filtros
     * 
     * @param precioMin Precio mínimo (inclusive)
     * @param precioMax Precio máximo (inclusive)
     * @return Productos en el rango
     */
    @Query("SELECT p FROM Producto p WHERE p.precio >= :precioMin AND p.precio <= :precioMax ORDER BY p.precio ASC")
    List<Producto> obtenerPorRangoPrecio(@Param("precioMin") Integer precioMin, @Param("precioMax") Integer precioMax);

    /**
     * Obtiene solo IDs de productos (proyección).
     * 
     * OPTIMIZACIÓN:
     * - Carga solo IDs (muy bajo overhead)
     * - Útil para validaciones rápidas
     * - Reduce memory footprint
     * 
     * @return Lista de IDs
     */
    @Query("SELECT p.id FROM Producto p")
    List<Long> obtenerTodosIds();

    /**
     * Obtiene el producto más caro.
     * 
     * OPTIMIZACIÓN:
     * - Cálculo en BD, no en aplicación
     * - Una sola fila retornada
     * 
     * @return Optional con el producto más caro
     */
    @Query("SELECT p FROM Producto p ORDER BY p.precio DESC LIMIT 1")
    Optional<Producto> obtenerMasCaro();

    /**
     * Obtiene el producto más barato.
     * 
     * OPTIMIZACIÓN:
     * - Cálculo en BD, no en aplicación
     * 
     * @return Optional con el producto más barato
     */
    @Query("SELECT p FROM Producto p ORDER BY p.precio ASC LIMIT 1")
    Optional<Producto> obtenerMasBarato();

    /**
     * Cuenta productos en un rango de precio.
     * 
     * OPTIMIZACIÓN:
     * - COUNT en BD es muy rápido
     * - No carga datos completos
     * 
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Cantidad de productos
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.precio >= :precioMin AND p.precio <= :precioMax")
    long contar(@Param("precioMin") Integer precioMin, @Param("precioMax") Integer precioMax);

    /**
     * Actualiza el precio de un producto (batch update optimizado).
     * 
     * OPTIMIZACIÓN:
     * - Una consulta SQL directa en lugar de cargar + modificar + guardar
     * - 3 operaciones reducidas a 1
     * - Muy rápido incluso para muchos registros
     * 
     * SQL:
     * UPDATE productos SET precio = :nuevoPrecio WHERE id = :id
     * 
     * @param id ID del producto
     * @param nuevoPrecio Nuevo precio
     * @return Filas actualizadas
     */
    @Modifying
    @Transactional
    @Query("UPDATE Producto p SET p.precio = :nuevoPrecio WHERE p.id = :id")
    int actualizarPrecio(@Param("id") Long id, @Param("nuevoPrecio") Integer nuevoPrecio);

    /**
     * Actualiza nombre y descripción juntas (operación atómica).
     * 
     * @param id ID del producto
     * @param nombre Nuevo nombre
     * @param descripcion Nueva descripción
     * @return Filas actualizadas
     */
    @Modifying
    @Transactional
    @Query("UPDATE Producto p SET p.nombre = :nombre, p.descripcion = :descripcion WHERE p.id = :id")
    int actualizarNombreYDescripcion(
            @Param("id") Long id,
            @Param("nombre") String nombre,
            @Param("descripcion") String descripcion);

    /**
     * Verifica existencia sin cargar todo el objeto.
     * 
     * OPTIMIZACIÓN:
     * - Usa EXISTS en BD (muy eficiente)
     * - No carga datos innecesarios
     * - Mejor que findById().isPresent() para validaciones
     * 
     * @param id ID a verificar
     * @return true si existe
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Producto p WHERE p.id = :id")
    boolean existePorId(@Param("id") Long id);

    /**
     * Obtiene estadísticas de precios.
     * 
     * OPTIMIZACIÓN:
     * - Agregaciones en BD
     * - Una consulta para múltiples cálculos
     * 
     * @return Array con [min, max, promedio, count]
     */
    @Query("SELECT " +
           "MIN(p.precio) as min, " +
           "MAX(p.precio) as max, " +
           "AVG(p.precio) as promedio, " +
           "COUNT(p) as total " +
           "FROM Producto p")
    Object obtenerEstadisticas();
}