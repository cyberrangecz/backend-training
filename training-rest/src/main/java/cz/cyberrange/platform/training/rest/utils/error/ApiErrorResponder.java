package cz.cyberrange.platform.training.rest.utils.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

/**
 * Assembles the response an exception handler returns: an {@link ApiError} carrying the reported
 * status, the message addressed to the caller and the exception's own message, sent as JSON with
 * the status held by that body.
 */
@Component
public class ApiErrorResponder {

  private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

  /**
   * Reports the path of the request being answered, as it is named in the error body.
   *
   * @param request the request being answered
   * @return the request URI
   */
  public String pathOf(HttpServletRequest request) {
    return URL_PATH_HELPER.getRequestUri(request);
  }

  /**
   * Builds the response carrying the error body.
   *
   * @param status the status to answer with, also written into the body
   * @param message the description addressed to the caller
   * @param error the exception's own message, carried in {@link ApiError#getErrors()}
   * @param path the path of the request being answered
   * @return the response holding the assembled body
   */
  public ResponseEntity<Object> respond(
      HttpStatus status, String message, String error, String path) {
    return asJson(ApiError.of(status, message, error, path));
  }

  /**
   * Wraps an error body in a response sent as JSON whatever media type the request accepts, with
   * the status held by that body.
   *
   * @param apiError the error body to send
   * @return the response holding the body
   */
  public static ResponseEntity<Object> asJson(ApiError apiError) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new ResponseEntity<>(apiError, headers, apiError.getStatus());
  }
}
