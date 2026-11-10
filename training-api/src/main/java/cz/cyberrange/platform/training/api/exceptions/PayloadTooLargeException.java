package cz.cyberrange.platform.training.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals a request whose content exceeds a size the service accepts. Mapped to HTTP 413 Content
 * Too Large by the {@code @ResponseStatus} on this type.
 */
@ResponseStatus(
    value = HttpStatus.PAYLOAD_TOO_LARGE,
    reason = "The request content is larger than the server is willing or able to process.")
public class PayloadTooLargeException extends RuntimeException {

  /**
   * Creates the exception with a message naming the exceeded limit.
   *
   * @param message the description reported to the caller
   */
  public PayloadTooLargeException(String message) {
    super(message);
  }
}
