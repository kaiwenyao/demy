package dev.kaiwen.common.exception;

/**
 * 请求参数不合法时抛出（HTTP 400）
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
