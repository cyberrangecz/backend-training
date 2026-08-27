package cz.cyberrange.platform.training.rest.utils.error;

import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.exceptions.errors.ApiSubError;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Error body built by {@link CustomRestExceptionHandlerTraining} for {@code
 * MicroserviceApiException}, carrying the failing microservice's own {@link ApiSubError} alongside
 * the fields inherited from {@link ApiError}.
 */
@ApiModel(
    value = "ApiMicroserviceError",
    description = "A detailed error information related to the microservice.",
    parent = ApiError.class)
public class ApiMicroserviceError extends ApiError {

  /** Error body returned by the microservice whose call raised the exception. */
  @ApiModelProperty(value = "Detailed error from another microservice.")
  private ApiSubError apiSubError;

  private ApiMicroserviceError() {
    super();
  }

  /**
   * Builds the error, falling back to {@link MicroserviceApiException}'s own
   * {@code @ResponseStatus} reason when {@code message} is {@code null}.
   */
  private ApiMicroserviceError(
      HttpStatus httpStatus, String message, String path, ApiSubError apiSubError) {
    super();
    this.setStatus(httpStatus);
    this.setMessage(
        message == null
            ? MicroserviceApiException.class.getAnnotation(ResponseStatus.class).reason()
            : message);
    this.setPath(path);
    this.setApiSubError(apiSubError);
    this.setTimestamp(System.currentTimeMillis());
  }

  /**
   * Builds the error body, falling back to {@link MicroserviceApiException}'s own
   * {@code @ResponseStatus} reason when {@code message} is {@code null}, and setting {@link
   * #getErrors()} directly from {@code errors}.
   *
   * @param httpStatus the failing microservice call's own status, reported and used for the HTTP
   *     response
   * @param message description of the error, or {@code null} to fall back to {@link
   *     MicroserviceApiException}'s own {@code @ResponseStatus} reason
   * @param errors the list stored as {@link #getErrors()}
   * @param path the request URI associated with the error
   * @param apiSubError the failing microservice's own error body
   * @return the built error body
   */
  public static ApiError of(
      HttpStatus httpStatus,
      String message,
      List<String> errors,
      String path,
      ApiSubError apiSubError) {
    ApiMicroserviceError apiMicroserviceError =
        new ApiMicroserviceError(httpStatus, message, path, apiSubError);
    apiMicroserviceError.setErrors(errors);
    return apiMicroserviceError;
  }

  /**
   * Builds the error body used by {@link CustomRestExceptionHandlerTraining} for {@code
   * MicroserviceApiException}, taking {@code httpStatus} from the failing microservice call's own
   * status rather than the exception's {@code @ResponseStatus}, and wrapping {@code error} as the
   * single element of {@link #getErrors()}.
   *
   * @param httpStatus the failing microservice call's own status, reported and used for the HTTP
   *     response
   * @param message description of the error, or {@code null} to fall back to {@link
   *     MicroserviceApiException}'s own {@code @ResponseStatus} reason
   * @param error the caught exception's own message, wrapped as the sole entry of {@link
   *     #getErrors()}
   * @param path the request URI associated with the error
   * @param apiSubError the failing microservice's own error body
   * @return the built error body
   */
  public static ApiError of(
      HttpStatus httpStatus, String message, String error, String path, ApiSubError apiSubError) {
    ApiMicroserviceError apiMicroserviceError =
        new ApiMicroserviceError(httpStatus, message, path, apiSubError);
    apiMicroserviceError.setError(error);
    return apiMicroserviceError;
  }

  public static ApiError of(
      HttpStatus httpStatus, String message, List<String> errors, ApiSubError apiSubError) {
    return ApiMicroserviceError.of(httpStatus, message, errors, "", apiSubError);
  }

  public static ApiError of(
      HttpStatus httpStatus, String message, String error, ApiSubError apiSubError) {
    return ApiMicroserviceError.of(httpStatus, message, error, "", apiSubError);
  }

  public ApiSubError getApiSubError() {
    return apiSubError;
  }

  public void setApiSubError(ApiSubError apiSubError) {
    this.apiSubError = apiSubError;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ApiMicroserviceError)) return false;
    if (!super.equals(o)) return false;
    ApiMicroserviceError that = (ApiMicroserviceError) o;
    return Objects.equals(getApiSubError(), that.getApiSubError());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getApiSubError());
  }

  @Override
  public String toString() {
    return "ApiMicroserviceError{"
        + "apiSubError="
        + apiSubError
        + ", timestamp="
        + getTimestamp()
        + ", status="
        + getStatus()
        + ", message='"
        + getMessage()
        + '\''
        + ", errors="
        + getErrors()
        + ", path='"
        + getPath()
        + '\''
        + '}';
  }
}
