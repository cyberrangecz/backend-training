package cz.muni.ics.kypo.training.security.exception;

public class SecurityException extends Exception{

    private static final long serialVersionUID = 1L;

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
