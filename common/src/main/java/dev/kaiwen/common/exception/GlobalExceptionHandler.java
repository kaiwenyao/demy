package dev.kaiwen.common.exception;

import dev.kaiwen.common.pojo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，统一返回 Result 格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public Result<Void> handleNotFound(ResourceNotFoundException e) {
        return Result.error(404, e.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public Result<Void> handleConflict(ResourceAlreadyExistsException e) {
        return Result.error(409, e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public Result<Void> handleBadRequest(BadRequestException e) {
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return Result.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected error", e);
        return Result.error(500, "Internal server error");
    }
}
