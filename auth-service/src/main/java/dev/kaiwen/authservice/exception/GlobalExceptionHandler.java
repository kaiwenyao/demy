package dev.kaiwen.authservice.exception;

import dev.kaiwen.common.response.Result;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Auth 专用异常处理，仅处理 common 未覆盖的异常（如 BadCredentialsException）。
 * ResourceNotFoundException、BadRequestException、MethodArgumentNotValidException 等由 common 的 GlobalExceptionHandler 处理。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleBadCredentials(BadCredentialsException e) {
        return Result.error(401, "Invalid credentials");
    }
}
