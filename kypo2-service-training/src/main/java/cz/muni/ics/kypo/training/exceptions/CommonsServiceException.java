package cz.muni.ics.kypo.training.exceptions;

public class CommonsServiceException extends RuntimeException {

    public CommonsServiceException() {
        super();
    }

    public CommonsServiceException(String s) {
        super(s);
    }

    public CommonsServiceException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public CommonsServiceException(Throwable throwable) {
        super(throwable);
    }
}
