package cz.muni.ics.kypo.training.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.MOVED_PERMANENTLY, reason = "This and all future requests should be directed to the given URI.")
public class MovedPermanentlyException extends RuntimeException {

    public MovedPermanentlyException() {
    }

    public MovedPermanentlyException(String message) {
        super(message);
    }

    public MovedPermanentlyException(String message, Throwable ex) {
        super(message, ex);
    }

    public MovedPermanentlyException(Throwable ex) {
        super(ex);
    }

}
