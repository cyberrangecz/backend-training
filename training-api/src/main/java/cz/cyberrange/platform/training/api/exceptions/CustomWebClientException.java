package cz.cyberrange.platform.training.api.exceptions;

import cz.cyberrange.platform.training.api.exceptions.errors.ApiSubError;
import org.springframework.http.HttpStatus;

/**
 * Signals that a call made through one of this service's {@code WebClient} instances to the
 * sandbox, user-and-group, or answers-storage microservice returned a 4xx or 5xx response. It is
 * caught by the calling service, either to branch on the response's status code or to be rewrapped
 * into a {@link MicroserviceApiException} for the training-rest error handler.
 */
public class CustomWebClientException extends RuntimeException {
  private HttpStatus statusCode;
  private ApiSubError apiSubError;

  /** Creates the exception carrying the given HTTP status and microservice error body */
  public CustomWebClientException(HttpStatus httpStatus, ApiSubError apiSubError) {
    super();
    this.apiSubError = apiSubError;
    this.statusCode = httpStatus;
  }

  /**
   * Returns the error body returned by the failing microservice call, deserialized as a {@link
   * cz.cyberrange.platform.training.api.exceptions.errors.JavaApiError} or {@link
   * cz.cyberrange.platform.training.api.exceptions.errors.PythonApiError} depending on which
   * microservice was called.
   */
  public ApiSubError getApiSubError() {
    return apiSubError;
  }

  /** Returns the HTTP status returned by the failing microservice call. */
  public HttpStatus getStatusCode() {
    return statusCode;
  }
}
