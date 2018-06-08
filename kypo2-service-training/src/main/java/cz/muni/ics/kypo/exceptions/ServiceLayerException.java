package cz.muni.ics.kypo.exceptions;

/**
 * @author Pavel Šeda (441048)
 *
 */
public class ServiceLayerException extends RuntimeException {
  public ServiceLayerException() {}

  public ServiceLayerException(String message) {
    super(message);
  }

  public ServiceLayerException(String message, Throwable ex) {
    super(message, ex);
  }

  public ServiceLayerException(Throwable ex) {
    super(ex);
  }

}
