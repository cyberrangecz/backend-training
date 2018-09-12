package cz.muni.ics.kypo.training.exceptions;

public class NoAvailableSandboxException extends RuntimeException {

	public NoAvailableSandboxException() {
		super();
	}

	public NoAvailableSandboxException(String s) {
		super(s);
	}

	public NoAvailableSandboxException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public NoAvailableSandboxException(Throwable throwable) {
		super(throwable);
	}
}
