package com.anexya.app.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class ApiExceptionHandler {
    record ErrorResponse(String message, Map<String, Object> details) {
    }

    @ExceptionHandler(TagReadNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleNotFound(TagReadNotFoundException ex) {
        return new ErrorResponse(ex.getMessage(), Map.of());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleValidation(Exception ex) {
        return new ErrorResponse("Validation failed", Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ErrorResponse("Invalid parameter", Map.of("parameter", ex.getName(), "value", String.valueOf(ex.getValue())));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleMissingParam(MissingServletRequestParameterException ex) {
        return new ErrorResponse("Missing required parameter", Map.of("parameter", ex.getParameterName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(new ErrorResponse("Internal server error", Map.of("error", ex.getMessage())));
    }
}
