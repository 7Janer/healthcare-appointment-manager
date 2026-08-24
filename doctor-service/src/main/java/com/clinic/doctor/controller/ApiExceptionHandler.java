package com.clinic.doctor.controller;
import org.springframework.http.*;import org.springframework.web.bind.MethodArgumentNotValidException;import org.springframework.web.bind.annotation.*;import org.springframework.web.client.RestClientException;import java.time.Instant;import java.util.*;
@RestControllerAdvice public class ApiExceptionHandler{
 @ExceptionHandler(NoSuchElementException.class) @ResponseStatus(HttpStatus.NOT_FOUND) Map<String,Object> missing(Exception e){return err("NOT_FOUND",e);}
 @ExceptionHandler(SecurityException.class) @ResponseStatus(HttpStatus.FORBIDDEN) Map<String,Object> forbidden(Exception e){return err("ACCESS_DENIED",e);}
 @ExceptionHandler({IllegalArgumentException.class,MethodArgumentNotValidException.class}) @ResponseStatus(HttpStatus.BAD_REQUEST) Map<String,Object> bad(Exception e){return err("INVALID_REQUEST",e);}
 @ExceptionHandler(RestClientException.class) @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE) Map<String,Object> downstream(Exception e){return err("LEAVE_SYNC_FAILED",new Exception("Could not safely process affected appointments; leave was not saved"));}
 private Map<String,Object> err(String c,Exception e){return Map.of("timestamp",Instant.now(),"code",c,"message",e.getMessage());}
}
