package cz.cyberrange.platform.training.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Signals that a requested resource does not exist, mapped to an HTTP 404 response */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The requested resource was not found")
public class ResourceNotFoundException extends RuntimeException {

  /** Creates the exception with no message */
  public ResourceNotFoundException() {}

  /** Creates the exception carrying the given message */
  public ResourceNotFoundException(String message) {
    super(message);
  }

  /** Creates the exception carrying the given message and cause */
  public ResourceNotFoundException(String message, Throwable ex) {
    super(message, ex);
  }

  /** Creates the exception carrying the given cause */
  public ResourceNotFoundException(Throwable ex) {
    super(ex);
  }
}
