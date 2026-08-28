package cz.cyberrange.platform.training.api.exceptions;

import cz.cyberrange.platform.training.api.exceptions.errors.ApiSubError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Signals that a call to an external microservice failed. The HTTP status returned to the client is
 * taken from {@link #getStatusCode()}, the failing service's own status, and the
 * {@code @ResponseStatus} on this type is not consulted at all.
 */
@ResponseStatus(
    reason = "Error when calling external service API. See the console for the detail reason.")
public class MicroserviceApiException extends RuntimeException {
  private HttpStatus statusCode;
  private ApiSubError apiSubError;

  /**
   * Builds the exception message by appending {@code apiSubError}'s own message to {@code message}
   */
  public MicroserviceApiException(String message, HttpStatus statusCode, ApiSubError apiSubError) {
    super(message + " " + apiSubError.getMessage());
    this.statusCode = statusCode;
    this.apiSubError = apiSubError;
  }

  /** Delegates to the message-building constructor with a generic default message */
  public MicroserviceApiException(HttpStatus statusCode, ApiSubError apiSubError) {
    this("Error when calling external microservice.", statusCode, apiSubError);
  }

  /**
   * Rebuilds the exception from the status code and sub-error carried by {@code
   * customWebClientException}
   */
  public MicroserviceApiException(
      String message, CustomWebClientException customWebClientException) {
    this(
        message,
        customWebClientException.getStatusCode(),
        customWebClientException.getApiSubError());
  }

  /** Returns the HTTP status returned by the failing microservice call */
  public HttpStatus getStatusCode() {
    return statusCode;
  }

  /** Returns the error body returned by the failing microservice call */
  public ApiSubError getApiSubError() {
    return apiSubError;
  }
}
