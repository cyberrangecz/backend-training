package cz.muni.ics.kypo.exceptions;

public class CannotBeUpdatedException extends RuntimeException{

  public CannotBeUpdatedException() {}

  public CannotBeUpdatedException(String message) {
    super(message);
  }

  public CannotBeUpdatedException(String message, Throwable ex) {
    super(message, ex);
  }

  public CannotBeUpdatedException(Throwable ex) {
    super(ex);
  }

}
