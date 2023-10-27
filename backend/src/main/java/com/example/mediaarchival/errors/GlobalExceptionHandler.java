package com.example.mediaarchival.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

/**
 * Provides global exception handling for the application.
 * Maps specific exceptions to appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles exceptions when a requested resource is not found.
   *
   * @param ex the ResourceNotFoundException thrown
   * @return a ResponseEntity with the error message and HTTP status code
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  /**
   * Handles exceptions when an operation cannot be completed due to active jobs.
   *
   * @param ex the ActiveJobsException thrown
   * @return a ResponseEntity with the error message and HTTP status code
   */
  @ExceptionHandler(ActiveJobsException.class)
  public ResponseEntity<Object> handleActiveJobsException(ActiveJobsException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
  }

  /**
   * Handles all other exceptions not explicitly managed by more specific handlers.
   *
   * @param ex the Exception thrown
   * @return a ResponseEntity with the error message and HTTP status code
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleAllExceptions(Exception ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handles AWS service exceptions.
   *
   * @param ex the AwsServiceException thrown
   * @return a ResponseEntity with the AWS error message and the mapped HTTP status code
   */
  @ExceptionHandler(AwsServiceException.class)
  public ResponseEntity<Object> handleAwsServiceException(AwsServiceException ex) {
    HttpStatus awsErrorStatus;
    try {
      awsErrorStatus = HttpStatus.valueOf(ex.statusCode());
    } catch (IllegalArgumentException e) {
      // If the AWS status code does not have a direct equivalent in HttpStatus,
      // fall back to an internal server error status code.
      awsErrorStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    return new ResponseEntity<>(ex.awsErrorDetails().errorMessage(), awsErrorStatus);
  }
}
