package cz.cyberrange.platform.training.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals an unexpected server-side failure with no more specific exception type to represent it.
 * Mapped to HTTP 500 Internal Server Error by the {@code @ResponseStatus} on this type.
 */
@ResponseStatus(
    value = HttpStatus.INTERNAL_SERVER_ERROR,
    reason =
        "A generic error message, given when an unexpected condition was encountered and no more specific message is suitable.")
public class InternalServerErrorException extends RuntimeException {

  public InternalServerErrorException() {}

  public InternalServerErrorException(String message) {
    super(message);
  }

  public InternalServerErrorException(String message, Throwable ex) {
    super(message, ex);
  }

  public InternalServerErrorException(Throwable e) {
    super(e);
  }
}
