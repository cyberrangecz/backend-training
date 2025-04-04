package cz.cyberrange.platform.training.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.TOO_EARLY, reason = "The requested resource is not yet ready")
public class ResourceNotReadyException extends ExceptionWithEntity {

    @Override
    protected String createDefaultReason(EntityErrorDetail entityErrorDetail) {
        return "The requested resource is not yet ready";
    }

    public ResourceNotReadyException(EntityErrorDetail entityErrorDetail) {
        super(entityErrorDetail);
    }

    public ResourceNotReadyException() {
    }
}
