package cz.muni.ics.kypo.training.exceptions;

import cz.muni.ics.kypo.training.exceptions.errors.ApiSubError;
import cz.muni.ics.kypo.training.exceptions.errors.JavaApiError;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "Error when calling UserAndGroup API")
public class MicroserviceApiException extends RuntimeException{
    private ApiSubError apiSubError;

    public MicroserviceApiException() {
        super();
    }

    public MicroserviceApiException(ApiSubError userAndGroupError) {
        super();
        this.apiSubError = userAndGroupError;

    }

    public MicroserviceApiException(String message, ApiSubError userAndGroupError) {
        super(message);
        this.apiSubError = userAndGroupError;
    }


    public MicroserviceApiException(ApiSubError userAndGroupError, Throwable cause) {
        super(cause);
        this.apiSubError = userAndGroupError;

    }

    public ApiSubError getApiSubError() {
        return apiSubError;
    }
}
