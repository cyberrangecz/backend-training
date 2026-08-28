package cz.cyberrange.platform.training.rest.utils.error;

import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;

/**
 * Error body built by {@link CustomRestExceptionHandlerTraining} for {@code
 * EntityNotFoundException}, {@code EntityConflictException}, {@code TooManyRequestsException} and
 * {@code UnprocessableEntityException}, carrying the exception's {@link EntityErrorDetail}
 * alongside the fields inherited from {@link ApiError}
 */
@ApiModel(
    value = "ApiEntityError",
    description = "A detailed error information related to the entity.",
    parent = ApiError.class)
public class ApiEntityError extends ApiError {
  /** Entity detail carried by the exception that produced this error, when the exception set one */
  @ApiModelProperty(value = "Detail of the entity which is related to the error.")
  private EntityErrorDetail entityErrorDetail;

  private ApiEntityError() {
    super();
  }

  /**
   * Builds the error, preferring {@code entityErrorDetail}'s own reason over {@code message} once
   * one is present
   */
  private ApiEntityError(
      HttpStatus httpStatus, String message, String path, EntityErrorDetail entityErrorDetail) {
    super();
    this.setStatus(httpStatus);
    this.setMessage(getMessage(entityErrorDetail, message));
    this.setPath(path);
    this.setTimestamp(System.currentTimeMillis());
    this.setEntityErrorDetail(entityErrorDetail);
  }

  /**
   * Builds the error body, preferring {@code entityErrorDetail}'s own reason over {@code message}
   * once one is present, and setting {@link #getErrors()} directly from {@code errors}.
   *
   * @param httpStatus the status reported in the body and used for the HTTP response
   * @param message fallback description used when {@code entityErrorDetail} carries no reason
   * @param errors the list stored as {@link #getErrors()}
   * @param path the request URI associated with the error
   * @param entityErrorDetail detail of the entity involved in the error, or {@code null}
   * @return the built error body
   */
  public static ApiEntityError of(
      HttpStatus httpStatus,
      String message,
      List<String> errors,
      String path,
      EntityErrorDetail entityErrorDetail) {
    ApiEntityError apiEntityError =
        new ApiEntityError(httpStatus, message, path, entityErrorDetail);
    apiEntityError.setErrors(errors);
    return apiEntityError;
  }

  /**
   * Builds the error body used by {@link CustomRestExceptionHandlerTraining} for {@code
   * EntityNotFoundException}, {@code EntityConflictException}, {@code TooManyRequestsException} and
   * {@code UnprocessableEntityException}, wrapping {@code error} as the single element of {@link
   * #getErrors()}.
   *
   * @param httpStatus the status reported in the body and used for the HTTP response
   * @param message fallback description used when {@code entityErrorDetail} carries no reason
   * @param error the caught exception's own message, wrapped as the sole entry of {@link
   *     #getErrors()}
   * @param path the request URI associated with the error
   * @param entityErrorDetail detail of the entity involved in the error, or {@code null}
   * @return the built error body
   */
  public static ApiEntityError of(
      HttpStatus httpStatus,
      String message,
      String error,
      String path,
      EntityErrorDetail entityErrorDetail) {
    ApiEntityError apiEntityError =
        new ApiEntityError(httpStatus, message, path, entityErrorDetail);
    apiEntityError.setError(error);
    return apiEntityError;
  }

  public static ApiEntityError of(
      HttpStatus httpStatus,
      String message,
      List<String> errors,
      EntityErrorDetail entityErrorDetail) {
    return ApiEntityError.of(httpStatus, message, errors, "", entityErrorDetail);
  }

  public static ApiEntityError of(
      HttpStatus httpStatus, String message, String error, EntityErrorDetail entityErrorDetail) {
    return ApiEntityError.of(httpStatus, message, error, "", entityErrorDetail);
  }

  private static String generateMessage(
      EntityErrorDetail entityErrorDetail, String defaultMessage) {
    if (entityErrorDetail != null
        && entityErrorDetail.getEntity() != null
        && entityErrorDetail.getIdentifier() != null) {
      return "Resource "
          + entityErrorDetail.getEntity()
          + " ("
          + entityErrorDetail.getIdentifier()
          + ": "
          + entityErrorDetail.getIdentifierValue()
          + ") not found.";
    } else if (entityErrorDetail != null
        && entityErrorDetail.getReason() != null
        && !entityErrorDetail.getReason().isBlank()) {
      return entityErrorDetail.getReason();
    } else {
      return defaultMessage;
    }
  }

  private static String getMessage(EntityErrorDetail entityErrorDetail, String defaultMessage) {
    if (entityErrorDetail == null) {
      return defaultMessage;
    }
    return entityErrorDetail.getReason() == null ? defaultMessage : entityErrorDetail.getReason();
  }

  public EntityErrorDetail getEntityErrorDetail() {
    return entityErrorDetail;
  }

  public void setEntityErrorDetail(EntityErrorDetail entityErrorDetail) {
    this.entityErrorDetail = entityErrorDetail;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ApiEntityError)) return false;
    if (!super.equals(o)) return false;
    ApiEntityError that = (ApiEntityError) o;
    return Objects.equals(getEntityErrorDetail(), that.getEntityErrorDetail());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getEntityErrorDetail());
  }

  @Override
  public String toString() {
    return "ApiEntityError{"
        + "entityErrorDetail="
        + entityErrorDetail
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
