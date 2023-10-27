package com.example.mediaarchival.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * Exception thrown when an operation cannot be performed on a library
 * due to active jobs that may conflict with the requested operation.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ActiveJobsException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ActiveJobsException(Long libraryId) {
    super("Cannot update library with ID " + libraryId + " because there are active jobs.");
  }

}
