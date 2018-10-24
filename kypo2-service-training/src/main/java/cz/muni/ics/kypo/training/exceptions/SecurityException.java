package cz.muni.ics.kypo.training.exceptions;

public class SecurityException extends RuntimeException {

	public SecurityException() {
		super();
	}

	public SecurityException(String s) {
		super(s);
	}

	public SecurityException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public SecurityException(Throwable throwable) {
		super(throwable);
	}
}
