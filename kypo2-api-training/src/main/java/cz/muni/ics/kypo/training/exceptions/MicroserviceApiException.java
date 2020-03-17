package cz.muni.ics.kypo.training.exceptions;

import cz.muni.ics.kypo.training.exceptions.errors.ApiSubError;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "Error when calling UserAndGroup API")
public class MicroserviceApiException extends RuntimeException{
    private ApiSubError apiSubError;

    public MicroserviceApiException() {
        super();
    }

    public MicroserviceApiException(ApiSubError apiSubError) {
        super();
        this.apiSubError = apiSubError;

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
