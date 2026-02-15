package dev.kaiwen.common.exception;

/**
 * 资源不存在时抛出（HTTP 404）
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
