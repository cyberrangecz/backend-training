package cz.muni.ics.kypo.training.exceptions;

/**
 * The type Rest template exception.
 */
public class RestTemplateException extends RuntimeException {
    private String statusCode;

    /**
     * Instantiates a new Rest template exception.
     */
    public RestTemplateException(){

    }

    /**
     * Instantiates a new Rest template exception.
     *
     * @param message    the message
     * @param statusCode the status code
     */
    public RestTemplateException(String message, String statusCode) {
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
    public RestTemplateException(String message, Throwable ex, String statusCode) {
        super(message, ex);
        this.statusCode = statusCode;
    }

    /**
     * Instantiates a new Rest template exception.
     *
     * @param ex the ex
     */
    public RestTemplateException(Throwable ex) {
        super(ex);
    }

    /**
     * Gets status code.
     *
     * @return the status code
     */
    public String getStatusCode() {
        return statusCode;
    }
}
