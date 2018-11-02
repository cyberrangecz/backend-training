package cz.muni.ics.kypo.training.exceptions;

/**
 * @author Dominik Pilar (445537)
 * <p>
 * NO_AVAILABLE_SANDBOX - no ready sandbox, which can be assigned to the participant
 * WRONG_LEVEL_TYPE - requested method cannot be call on given level because of missing atribute
 * RESOURCE_NOT_FOUND - resource is not in DB
 * NO_NEXT_LEVEL - given level has no next level and this request should not be called
 * UNEXPECTED_ERROR - unexpected error, after some REST request
 * RESOURCE_CONFLICT - the resource cannot be modified, deleted or created because of any reason
 * SECURITY_RIGHTS - client has not necessary permission
 */
public enum ErrorCode {
    NO_AVAILABLE_SANDBOX, WRONG_LEVEL_TYPE, RESOURCE_NOT_FOUND,
    NO_NEXT_LEVEL, UNEXPECTED_ERROR, RESOURCE_CONFLICT, SECURITY_RIGHTS
}
