package com.northstar.crm.api;

import com.northstar.crm.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String DEFAULT_CORRELATION_ID = "lab-request-001";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
    List<ErrorResponse.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> new ErrorResponse.FieldViolation(fe.getField(), fe.getDefaultMessage()))
        .sorted(Comparator.comparing(ErrorResponse.FieldViolation::getField))
        .toList();

    ErrorResponse body = envelope(HttpStatus.BAD_REQUEST, "Validation failed", request);
    body.setViolations(violations);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(IllegalArgumentException ex, WebRequest request) {
    ErrorResponse body = envelope(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleConflict(IllegalStateException ex, WebRequest request) {
    ErrorResponse body = envelope(HttpStatus.CONFLICT, ex.getMessage(), request);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleSafe500(Exception ex, WebRequest request) {
    ErrorResponse body = envelope(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request);
    log.error("Unhandled exception, correlationId={}", body.getCorrelationId(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  private ErrorResponse envelope(HttpStatus status, String message, WebRequest request) {
    ErrorResponse body = new ErrorResponse();
    body.setStatus(status.value());
    body.setError(status.getReasonPhrase());
    body.setMessage(message);
    body.setCorrelationId(correlationId(request));
    body.setViolations(List.of());
    return body;
  }

  private String correlationId(WebRequest request) {
    String header = request.getHeader("X-Correlation-Id");
    return (header == null || header.isBlank()) ? DEFAULT_CORRELATION_ID : header;
  }
}
