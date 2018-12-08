package cz.muni.ics.kypo.training.persistence.exceptions;

/**
 * @author Pavel Seda
 */
public class PersistenceCommonsException extends RuntimeException {
    
    public PersistenceCommonsException() {
        super();
    }

    public PersistenceCommonsException(String s) {
        super(s);
    }

    public PersistenceCommonsException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public PersistenceCommonsException(Throwable throwable) {
        super(throwable);
    }
}
