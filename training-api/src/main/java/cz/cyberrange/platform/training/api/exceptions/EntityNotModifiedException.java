package cz.cyberrange.platform.training.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_MODIFIED, reason = "The requested entity was not modified")
public class EntityNotModifiedException extends ExceptionWithEntity {

  public EntityNotModifiedException() {
    super();
  }

  public EntityNotModifiedException(EntityErrorDetail entityErrorDetail) {
    super(entityErrorDetail);
  }

  public EntityNotModifiedException(EntityErrorDetail entityErrorDetail, Throwable cause) {
    super(entityErrorDetail, cause);
  }

  public EntityNotModifiedException(Throwable cause) {
    super(cause);
  }

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
    reason.append(" was not modified.");
    return reason.toString();
  }
}
