package cz.cyberrange.platform.training.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals that the caller has exceeded a rate limit while requesting an entity. Mapped to HTTP 429
 * Too Many Requests by the {@code @ResponseStatus} on this type.
 */
@ResponseStatus(
    value = HttpStatus.TOO_MANY_REQUESTS,
    reason = "The user has sent too many requests in a given amount of time (\"rate limiting\").")
public class TooManyRequestsException extends ExceptionWithEntity {

  public TooManyRequestsException() {
    super();
  }

  public TooManyRequestsException(EntityErrorDetail entityErrorDetail) {
    super(entityErrorDetail);
  }

  public TooManyRequestsException(EntityErrorDetail entityErrorDetail, Throwable cause) {
    super(entityErrorDetail, cause);
  }

  public TooManyRequestsException(Throwable cause) {
    super(cause);
  }

  /**
   * Builds a reason naming the rate-limited entity's class, and, when both are present, the
   * identifier label and value carried on {@code entityErrorDetail}
   */
  protected String createDefaultReason(EntityErrorDetail entityErrorDetail) {
    StringBuilder reason =
        new StringBuilder("User has sent too many requests to obtain entity ")
            .append(entityErrorDetail.getEntity());
    if (entityErrorDetail.getIdentifier() != null
        && entityErrorDetail.getIdentifierValue() != null) {
      reason
          .append(" (")
          .append(entityErrorDetail.getIdentifier())
          .append(": ")
          .append(entityErrorDetail.getIdentifierValue())
          .append(")");
    }
    reason.append(".");
    return reason.toString();
  }
}
