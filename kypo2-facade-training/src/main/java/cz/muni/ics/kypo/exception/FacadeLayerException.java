package cz.muni.ics.kypo.exception;

/**
 * @author Pavel Šeda (441048)
 *
 */
public class FacadeLayerException extends RuntimeException {
  public FacadeLayerException() {}

  public FacadeLayerException(String message) {
    super(message);
  }

  public FacadeLayerException(String message, Throwable ex) {
    super(message, ex);
  }

  public FacadeLayerException(Throwable ex) {
    super(ex);
  }

}
