package com.dating.gateway.exception;

import com.dating.gateway.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一 REST 异常出口：业务异常返回 HTTP 200 + Result.code；系统异常返回 500。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GatewayBizException.class)
    public Result<Void> handleGatewayBizException(GatewayBizException ex) {
        return new Result<>(ex.getErrorCode().getCode(), ex.getDetailMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : GatewayErrorCode.INVALID_ARGUMENT.getMessage();
        return new Result<>(GatewayErrorCode.INVALID_ARGUMENT.getCode(), message, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpectedException(Exception ex) {
        log.error("未预期系统异常", ex);
        Result<Void> body = new Result<>(10999, "系统繁忙，请稍后重试", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
