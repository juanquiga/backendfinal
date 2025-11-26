package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para Pedido.
 * Separa la representación interna del modelo de la API.
 * Incluye validaciones para asegurar datos consistentes.
 */
public class PedidoDTO {
    
    private Long id;

    @NotBlank(message = "El nombre del cliente es requerido")
    private String nombreCliente;

    @NotBlank(message = "El teléfono es requerido")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
    private String telefono;

    @NotBlank(message = "La dirección es requerida")
    private String direccion;

    private Integer total;

    @NotEmpty(message = "El pedido debe contener al menos un item")
    private String itemsJson;

    private String estado;

    // Constructores
    public PedidoDTO() {}

    public PedidoDTO(Long id, String nombreCliente, String telefono, String direccion, 
                     Integer total, String itemsJson, String estado) {
        this.id = id;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.direccion = direccion;
        this.total = total;
        this.itemsJson = itemsJson;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getItemsJson() {
        return itemsJson;
    }

    public void setItemsJson(String itemsJson) {
        this.itemsJson = itemsJson;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
