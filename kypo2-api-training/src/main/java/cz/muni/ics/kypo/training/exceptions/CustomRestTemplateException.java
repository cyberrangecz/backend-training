package cz.muni.ics.kypo.training.exceptions;

import cz.muni.ics.kypo.training.exceptions.errors.ApiSubError;
import org.springframework.http.HttpStatus;

/**
 * The type Rest template exception.
 */
public class CustomRestTemplateException extends RuntimeException {
    private HttpStatus statusCode;
    private ApiSubError apiSubError;

    /**
     * Instantiates a new Rest template exception.
     *
     * @param message    the message
     * @param statusCode the status code
     */
    public CustomRestTemplateException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Instantiates a new Rest template exception.
     *
     * @param message    the message
     * @param ex         the ex
     * @param statusCode the status code
     */
    public CustomRestTemplateException(String message, Throwable ex, HttpStatus statusCode) {
        super(message, ex);
        this.statusCode = statusCode;
    }

    /**
     * Instantiates a new Rest template exception.
     *
     * @param apiSubError detailed information about error.
     */
    public CustomRestTemplateException(ApiSubError apiSubError) {
        super();
        this.apiSubError = apiSubError;
        this.statusCode = apiSubError.getStatus();
    }

    /**
     * Instantiates a new Rest template exception.
     *
     * @param message    the message
     * @param apiSubError detailed information about error
     */
    public CustomRestTemplateException(String message, ApiSubError apiSubError) {
        super(message);
        this.apiSubError = apiSubError;
        this.statusCode = apiSubError.getStatus();
    }

    /**
     * Instantiates a new Rest template exception.
     *
     * @param message    the message
     * @param ex         the ex
     * @param apiSubError detailed information about error
     */
    public CustomRestTemplateException(String message, Throwable ex, ApiSubError apiSubError) {
        super(message, ex);
        this.apiSubError = apiSubError;
        this.statusCode = apiSubError.getStatus();
    }

    /**
     * Gets detailed information about error.
     *
     * @return detailed information about error
     */
    public ApiSubError getApiSubError() {
        return apiSubError;
    }

    /**
     * Gets status code.
     *
     * @return the status code
     */
    public HttpStatus getStatusCode() {
        return statusCode;
    }
}
