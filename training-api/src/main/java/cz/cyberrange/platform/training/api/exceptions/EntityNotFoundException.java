package cz.cyberrange.platform.training.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals that a requested entity does not exist. Mapped to HTTP 404 Not Found by the
 * {@code @ResponseStatus} on this type.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The requested entity could not be found")
public class EntityNotFoundException extends ExceptionWithEntity {
  public EntityNotFoundException() {
    super();
  }

  public EntityNotFoundException(EntityErrorDetail entityErrorDetail) {
    super(entityErrorDetail);
  }

  public EntityNotFoundException(EntityErrorDetail entityErrorDetail, Throwable cause) {
    super(entityErrorDetail, cause);
  }

  public EntityNotFoundException(Throwable cause) {
    super(cause);
  }

  /**
   * Builds a reason naming the missing entity's class, and, when both are present, the identifier
   * label and value carried on {@code entityErrorDetail}.
   */
  protected String createDefaultReason(EntityErrorDetail entityErrorDetail) {
    StringBuilder reason = new StringBuilder("Entity ").append(entityErrorDetail.getEntity());
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
