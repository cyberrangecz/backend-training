package cz.muni.ics.kypo.training.exception;

public class CommonsFacadeException extends RuntimeException {

    public CommonsFacadeException() {
        super();
    }

    public CommonsFacadeException(String s) {
        super(s);
    }

    public CommonsFacadeException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public CommonsFacadeException(Throwable throwable) {
        super(throwable);
    }
}
