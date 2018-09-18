package cz.muni.ics.kypo.training.exceptions;

public class CannotBeDeletedException extends RuntimeException {

	public CannotBeDeletedException() {}

	public CannotBeDeletedException(String message) {
		super(message);
	}

	public CannotBeDeletedException(String message, Throwable ex) {
		super(message, ex);
	}

	public CannotBeDeletedException(Throwable ex) {
		super(ex);
	}

}
