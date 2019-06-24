package cz.muni.ics.kypo.training.exceptions;

/**
 * The type Service layer exception.
 *
 * @author Pavel Šeda (441048)
 */
public class ServiceLayerException extends RuntimeException {
    private final ErrorCode code;

    /**
     * Instantiates a new Service layer exception with specified error code.
     *
     * @param code the code
     */
    public ServiceLayerException(ErrorCode code) {
        this.code = code;
    }

    /**
     * Instantiates a new Service layer exception with the specified detail message.
     *
     * @param message the message
     * @param code    the code
     */
    public ServiceLayerException(String message, ErrorCode code) {
        super(message);
        this.code = code;
    }

    /**
     * Instantiates a new Service layer exception with the specified detail message and error code.
     *
     * @param message the message
     * @param ex      the ex
     * @param code    the code
     */
    public ServiceLayerException(String message, Throwable ex, ErrorCode code) {
        super(message, ex);
        this.code = code;
    }

    /**
     * Instantiates a new Service layer exception with the specified error code.
     *
     * @param ex   the ex
     * @param code the code
     */
    public ServiceLayerException(Throwable ex, ErrorCode code) {
        super(ex);
        this.code = code;
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public ErrorCode getCode() {
        return code;
    }
}
