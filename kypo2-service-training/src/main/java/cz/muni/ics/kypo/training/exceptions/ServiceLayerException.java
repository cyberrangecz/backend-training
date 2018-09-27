package cz.muni.ics.kypo.training.exceptions;

/**
 * @author Pavel Šeda (441048)
 *
 */
public class ServiceLayerException extends RuntimeException {
  private final ErrorCode code;

  public ServiceLayerException(ErrorCode code) { this.code = code;}

  public ServiceLayerException(String message, ErrorCode code) {
    super(message);
    this.code = code;
  }

  public ServiceLayerException(String message, Throwable ex, ErrorCode code) {
    super(message, ex);
    this.code = code;
  }

  public ServiceLayerException(Throwable ex, ErrorCode code) {
    super(ex);
    this.code = code;
  }

  public ErrorCode getCode() {
    return code;
  }
}
