package com.clinic.auth.controller;

import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BadCredentialsException.class) @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Map<String,Object> badCredentials(Exception e) { return error("AUTHENTICATION_FAILED", e); }
    @ExceptionHandler(AccessDeniedException.class) @ResponseStatus(HttpStatus.FORBIDDEN)
    Map<String,Object> forbidden(Exception e) { return error("ACCESS_DENIED", e); }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class}) @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String,Object> badRequest(Exception e) { return error("INVALID_REQUEST", e); }
    private Map<String,Object> error(String code, Exception e) { return Map.of("timestamp", Instant.now(), "code", code, "message", e.getMessage()); }
}
