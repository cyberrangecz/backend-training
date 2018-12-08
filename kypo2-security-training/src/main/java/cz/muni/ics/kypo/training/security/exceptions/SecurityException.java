package cz.muni.ics.kypo.training.security.exceptions;

/**
 * @author Pavel Seda
 */
public class SecurityException extends Exception {

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
