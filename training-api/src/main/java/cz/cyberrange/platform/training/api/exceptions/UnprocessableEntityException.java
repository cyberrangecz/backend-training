package cz.cyberrange.platform.training.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals that an entity's data cannot be processed as given. Mapped to HTTP 422 Unprocessable
 * Entity by the {@code @ResponseStatus} on this type.
 */
@ResponseStatus(
    value = HttpStatus.UNPROCESSABLE_ENTITY,
    reason = "The requested data cannot be processed.")
public class UnprocessableEntityException extends ExceptionWithEntity {

  public UnprocessableEntityException() {
    super();
  }

  public UnprocessableEntityException(EntityErrorDetail entityErrorDetail) {
    super(entityErrorDetail);
  }

  public UnprocessableEntityException(EntityErrorDetail entityErrorDetail, Throwable cause) {
    super(entityErrorDetail, cause);
  }

  public UnprocessableEntityException(Throwable cause) {
    super(cause);
  }

  /**
   * Builds a reason naming the unprocessable entity's class, and, when both are present, the
   * identifier label and value carried on {@code entityErrorDetail}.
   */
  protected String createDefaultReason(EntityErrorDetail entityErrorDetail) {
    StringBuilder reason =
        new StringBuilder("Unable to be process entity ").append(entityErrorDetail.getEntity());
    if (entityErrorDetail.getIdentifier() != null
        && entityErrorDetail.getIdentifierValue() != null) {
      reason
          .append(" (")
          .append(entityErrorDetail.getIdentifier())
          .append(": ")
          .append(entityErrorDetail.getIdentifierValue())
          .append(")");
    }
    reason.append(" not found.");
    return reason.toString();
  }
}
