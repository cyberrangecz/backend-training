package cz.muni.ics.kypo.training.rest;

import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.rest.exceptions.*;
import org.springframework.security.access.AccessDeniedException;


public class ExceptionSorter {
    private ExceptionSorter() {
        throw new IllegalStateException("Util class");
    }

    public static RuntimeException throwException(RuntimeException ex) {
        switch (((ServiceLayerException) ex.getCause()).getCode()) {
            case WRONG_LEVEL_TYPE:
                return new BadRequestException(ex);
            case RESOURCE_NOT_FOUND:
            case NO_NEXT_LEVEL:
                return new ResourceNotFoundException(ex);
            case RESOURCE_CONFLICT:
                return new ConflictException(ex);
            case NO_AVAILABLE_SANDBOX:
                return new ServiceUnavailableException(ex);
            case SECURITY_RIGHTS:
                return new AccessDeniedException("Access is denied.");
            case TR_ACQUIRED_LOCK:
                return new TooManyRequestsException(ex);
            case UNEXPECTED_ERROR:
            default:
                return new InternalServerErrorException(ex);
        }
    }

}

