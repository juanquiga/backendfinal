package com.example.backend.exception;

/**
 * Excepción personalizada para cuando no se encuentra un recurso.
 * Se lanza cuando se intenta acceder a un recurso que no existe.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s no encontrado con %s: %s", resourceName, fieldName, fieldValue));
    }
}
