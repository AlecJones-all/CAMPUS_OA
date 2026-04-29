package com.campusoa.common;

import com.campusoa.auth.exception.AuthException;
import com.campusoa.business.exception.BusinessException;
import com.campusoa.system.exception.SystemException;
import com.campusoa.workflow.exception.WorkflowException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleAuthException(AuthException exception, HttpServletRequest request) {
        log.warn("Auth request failed. method={}, path={}, message={}",
                request.getMethod(), request.getRequestURI(), exception.getMessage());
        return new ApiResponse<>(false, exception.getMessage(), null);
    }

    @ExceptionHandler(WorkflowException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleWorkflowException(WorkflowException exception, HttpServletRequest request) {
        log.warn("Workflow request failed. method={}, path={}, message={}",
                request.getMethod(), request.getRequestURI(), exception.getMessage());
        return new ApiResponse<>(false, exception.getMessage(), null);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        log.warn("Business request failed. method={}, path={}, message={}",
                request.getMethod(), request.getRequestURI(), exception.getMessage());
        return new ApiResponse<>(false, exception.getMessage(), null);
    }

    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleSystemException(SystemException exception, HttpServletRequest request) {
        log.warn("System request failed. method={}, path={}, message={}",
                request.getMethod(), request.getRequestURI(), exception.getMessage());
        return new ApiResponse<>(false, exception.getMessage(), null);
    }

    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleJdbcConnectionException(
            CannotGetJdbcConnectionException exception,
            HttpServletRequest request
    ) {
        log.error("Database connection failed. method={}, path={}",
                request.getMethod(), request.getRequestURI(), exception);
        return new ApiResponse<>(false,
                "数据库连接失败，请检查后端数据库配置和本地 MySQL 服务状态",
                null);
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleDataAccessException(DataAccessException exception, HttpServletRequest request) {
        log.error("Database access failed. method={}, path={}",
                request.getMethod(), request.getRequestURI(), exception);
        return new ApiResponse<>(false,
                "数据库操作失败，请检查运行库结构是否与当前初始化脚本一致",
                null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "请求参数错误" : error.getDefaultMessage())
                .orElse("请求参数错误");
        log.warn("Validation failed. method={}, path={}, message={}",
                request.getMethod(), request.getRequestURI(), message);
        return new ApiResponse<>(false, message, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        log.warn("Request body unreadable. method={}, path={}, message={}",
                request.getMethod(), request.getRequestURI(), exception.getMessage());
        return new ApiResponse<>(false, "请求体格式错误，请提交合法 JSON", null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled backend exception. method={}, path={}",
                request.getMethod(), request.getRequestURI(), exception);
        return new ApiResponse<>(false, "服务器内部错误，请查看后端日志", null);
    }
}
