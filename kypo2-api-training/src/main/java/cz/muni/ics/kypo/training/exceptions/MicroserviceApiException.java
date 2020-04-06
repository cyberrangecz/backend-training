package cz.muni.ics.kypo.training.exceptions;

import cz.muni.ics.kypo.training.exceptions.errors.ApiSubError;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;

@ResponseStatus(reason = "Error when calling external service API")
public class MicroserviceApiException extends RuntimeException{
    private ApiSubError apiSubError;

    public MicroserviceApiException() {
        super();
    }

    public MicroserviceApiException(ApiSubError apiSubError) {
        super();
        this.apiSubError = apiSubError;

    }

    public MicroserviceApiException(String message) {
        super(message);
    }

    public MicroserviceApiException(String message, ConstraintViolationException exception) {
        super(message + " Constraint violations: " + exception.getConstraintViolations().toString());

    }

    public MicroserviceApiException(String message, ApiSubError apiSubError) {
        super(message);
        this.apiSubError = apiSubError;
    }


    public MicroserviceApiException(ApiSubError apiSubError, Throwable cause) {
        super(cause);
        this.apiSubError = apiSubError;

    }

    public ApiSubError getApiSubError() {
        return apiSubError;
    }
}
