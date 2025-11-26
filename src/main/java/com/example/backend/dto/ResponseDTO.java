package com.example.backend.dto;

/**
 * DTO genérico para respuestas API.
 * Proporciona una estructura consistente para todos los endpoints.
 * Mejora la experiencia del cliente al mantener formato uniforme.
 */
public class ResponseDTO<T> {
    
    private boolean success;
    private String message;
    private T data;
    private Long timestamp;

    // Constructores
    public ResponseDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    public ResponseDTO(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }

    public ResponseDTO(boolean success, String message, T data) {
        this();
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Métodos factory para crear respuestas comunes
    public static <T> ResponseDTO<T> success(String message, T data) {
        return new ResponseDTO<>(true, message, data);
    }

    public static <T> ResponseDTO<T> success(String message) {
        return new ResponseDTO<>(true, message);
    }

    public static <T> ResponseDTO<T> error(String message) {
        return new ResponseDTO<>(false, message);
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
