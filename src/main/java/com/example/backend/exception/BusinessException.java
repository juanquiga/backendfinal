package com.example.backend.exception;

/**
 * Excepción personalizada para errores de lógica de negocio.
 * Se utiliza cuando las reglas de negocio no se cumplen.
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
