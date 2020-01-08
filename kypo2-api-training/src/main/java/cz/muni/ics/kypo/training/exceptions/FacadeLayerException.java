package cz.muni.ics.kypo.training.exceptions;

/**
 * The type Facade layer exception.
 *
 */
public class FacadeLayerException extends RuntimeException {

    /**
     * Instantiates a new Facade layer exception.
     */
    public FacadeLayerException() {
    }

    /**
     * Instantiates a new Facade layer exception with the specified detail message.
     *
     * @param message the message
     */
    public FacadeLayerException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Facade layer exception with the specified detail message and exception.
     *
     * @param message the message
     * @param ex      the ex
     */
    public FacadeLayerException(String message, Throwable ex) {
        super(message, ex);
    }

    /**
     * Instantiates a new Facade layer exception with the specified exception.
     *
     * @param ex the ex
     */
    public FacadeLayerException(Throwable ex) {
        super(ex);
    }

}
