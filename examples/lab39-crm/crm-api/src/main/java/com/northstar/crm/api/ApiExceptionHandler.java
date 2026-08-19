package com.northstar.crm.api;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  private static final String CORRELATION_ID = "lab-request-001";

  @ExceptionHandler(FieldValidationException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(FieldValidationException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", ex.getMessage(), "fieldErrors", ex.getFieldErrors()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
  }

  /**
   * Step 11. The service checks existsByEmail first, so this is the race: another
   * request inserted the same email between the check and the insert, and the
   * UNIQUE constraint is the only thing that can actually stop it. The message is
   * ours, not the driver's -- ex.getMessage() would carry the SQLSTATE, the
   * constraint name and the table name straight to the client.
   */
  @ExceptionHandler(DuplicateCustomerException.class)
  ResponseEntity<ProblemDetail> duplicateCustomer(DuplicateCustomerException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(problem("Email already registered", ex.getCorrelationId()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ProblemDetail> duplicate(DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(problem("Request conflicts with an existing record", CORRELATION_ID));
  }

  /**
   * A stale @Version lost the race: the row was updated by someone else between
   * this request's read and its write. 409 rather than 500 because nothing is
   * broken, and the client's move is to reload and reapply rather than retry the
   * same stale payload.
   */
  @ExceptionHandler(OptimisticLockingFailureException.class)
  ResponseEntity<ProblemDetail> conflict(OptimisticLockingFailureException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(problem("Record was modified by another request, reload and retry", CORRELATION_ID));
  }

  private ProblemDetail problem(String detail, String correlationId) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problem.setTitle("Conflict");
    problem.setDetail(detail);
    problem.setProperty("correlationId", correlationId);
    return problem;
  }
}
