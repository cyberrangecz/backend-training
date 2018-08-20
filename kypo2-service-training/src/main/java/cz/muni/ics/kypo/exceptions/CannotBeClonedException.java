package cz.muni.ics.kypo.exceptions;

public class CannotBeClonedException extends RuntimeException {

  public CannotBeClonedException() {}

  public CannotBeClonedException(String message) {
    super(message);
  }

  public CannotBeClonedException(String message, Throwable ex) {
    super(message, ex);
  }

  public CannotBeClonedException(Throwable ex) {
    super(ex);
  }
}
