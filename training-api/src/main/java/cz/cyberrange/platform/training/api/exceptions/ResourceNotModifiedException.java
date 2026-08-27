package cz.cyberrange.platform.training.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Signals that a requested resource has not changed, mapped to an HTTP 304 response */
@ResponseStatus(value = HttpStatus.NOT_MODIFIED, reason = "The requested resource was not modified")
public class ResourceNotModifiedException extends RuntimeException {

  /** Creates the exception with no message */
  public ResourceNotModifiedException() {}

  /** Creates the exception carrying the given message */
  public ResourceNotModifiedException(String message) {
    super(message);
  }

  /** Creates the exception carrying the given message and cause */
  public ResourceNotModifiedException(String message, Throwable ex) {
    super(message, ex);
  }

  /** Creates the exception carrying the given cause */
  public ResourceNotModifiedException(Throwable ex) {
    super(ex);
  }
}
