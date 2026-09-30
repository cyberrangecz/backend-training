package cz.cyberrange.platform.training.rest.utils.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;

/**
 * Body of an error response written by {@link CustomRestExceptionHandlerTraining}. Every
 * {@code @ExceptionHandler} and overridden {@link
 * org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler} method in
 * that class builds one of these (or the {@link ApiEntityError} / {@link ApiMicroserviceError}
 * subtype) and returns it as the {@link org.springframework.http.ResponseEntity} body, with {@link
 * #getStatus()} also supplying that response's HTTP status.
 */
@ApiModel(
    value = "ApiError",
    subTypes = {ApiEntityError.class, ApiMicroserviceError.class},
    description = "Superclass for classes ApiEntityError and ApiMicroserviceError")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ApiEntityError.class, name = "ApiEntityError"),
  @JsonSubTypes.Type(value = ApiMicroserviceError.class, name = "ApiMicroserviceError")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

  /** Epoch millisecond at which the handler that produced this error ran */
  @ApiModelProperty(
      value = "The time when the exception occurred",
      example = "1574062900 (different for each type of exception)")
  private long timestamp;

  /**
   * Status carried in the error body, which the handler also sets as the actual HTTP response
   * status
   */
  @ApiModelProperty(
      value = "The HTTP response status code",
      example = "404 Not found (different for each type of exception).")
  private HttpStatus status;

  /**
   * Description of the error. Its source varies by handler method: the deepest cause's message in
   * the caught exception's chain on most handlers, the exception class's {@code @ResponseStatus}
   * reason text (overridden by an {@code EntityErrorDetail} reason when the exception carries one)
   * on the {@code ApiEntityError} handlers, or the joined validation messages on {@link
   * CustomRestExceptionHandlerTraining#handleMethodArgumentNotValid}.
   */
  @ApiModelProperty(
      value = "The specific description of the ApiError.",
      example =
          "The IDMGroup could not be found in database (different for each type of exception).")
  private String message;

  /**
   * Single-element list holding the caught exception's own message, set through {@link
   * #setError(String)}
   */
  @ApiModelProperty(
      value = "The list of main reasons of the ApiError.",
      example = "[The requested resource was not found (different for each type of exception).]")
  private List<String> errors;

  /**
   * Request path associated with the error. Its source varies by handler method: the request's
   * context path on the overridden {@link
   * org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler} methods
   * and on {@link CustomRestExceptionHandlerTraining#handleAuthenticationException}, or the full
   * request URI on the other custom {@code @ExceptionHandler} methods.
   */
  @ApiModelProperty(
      value = "The requested URI path which caused error.",
      example = "/user-and-group/api/v1/groups/1000 (different for each type of exception).")
  private String path;

  protected ApiError() {}

  private ApiError(HttpStatus httpStatus, String message, String path) {
    this.status = httpStatus;
    this.message = message;
    this.path = path;
    this.timestamp = System.currentTimeMillis();
  }

  /**
   * Builds the error body, setting {@link #getErrors()} directly from {@code errors}.
   *
   * @param httpStatus the status reported in the body and used for the HTTP response
   * @param message description of the error
   * @param errors the list stored as {@link #getErrors()}
   * @param path the request path or context path associated with the error
   * @return the built error body
   */
  public static ApiError of(
      HttpStatus httpStatus, String message, List<String> errors, String path) {
    ApiError apiError = new ApiError(httpStatus, message, path);
    apiError.setErrors(errors);
    return apiError;
  }

  /**
   * Builds the error body returned by {@link CustomRestExceptionHandlerTraining}, setting {@code
   * httpStatus} both as the reported status and, by the caller, as the response's actual HTTP
   * status, and wrapping {@code error} as the single element of {@link #getErrors()}.
   *
   * @param httpStatus the status reported in the body and used for the HTTP response
   * @param message description of the error
   * @param error the caught exception's own message, wrapped as the sole entry of {@link
   *     #getErrors()}
   * @param path the request path or context path associated with the error
   * @return the built error body
   */
  public static ApiError of(HttpStatus httpStatus, String message, String error, String path) {
    ApiError apiError = new ApiError(httpStatus, message, path);
    apiError.setError(error);
    return apiError;
  }

  public static ApiError of(HttpStatus httpStatus, String message, List<String> errors) {
    return ApiError.of(httpStatus, message, errors, "");
  }

  public static ApiError of(HttpStatus httpStatus, String message, String error) {
    return ApiError.of(httpStatus, message, error, "");
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public void setStatus(final HttpStatus status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(final List<String> errors) {
    this.errors = errors;
  }

  /** Replaces {@link #getErrors()} with a single-element list holding {@code error} */
  public void setError(final String error) {
    errors = Arrays.asList(error);
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String toString() {
    return "ApiError{"
        + "timestamp="
        + timestamp
        + ", status="
        + status
        + ", message='"
        + message
        + '\''
        + ", errors="
        + errors
        + ", path='"
        + path
        + '\''
        + '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, status, message, errors, path);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof ApiError)) return false;
    ApiError other = (ApiError) obj;
    return Objects.equals(errors, other.getErrors())
        && Objects.equals(message, other.getMessage())
        && Objects.equals(path, other.getPath())
        && Objects.equals(status, other.getStatus())
        && Objects.equals(timestamp, other.getTimestamp());
  }
}
