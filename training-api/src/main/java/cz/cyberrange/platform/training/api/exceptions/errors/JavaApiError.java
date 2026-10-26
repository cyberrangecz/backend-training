package cz.cyberrange.platform.training.api.exceptions.errors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;

/**
 * The error body returned by a failing call to the user-and-group or answers-storage microservice,
 * deserialized by {@code ObjectMapper.readValue} from that response's JSON. The {@link
 * #entityErrorDetail} field can likewise be populated by that deserialization, but nothing in this
 * codebase reads it back out through {@link #getEntityErrorDetail()}.
 */
@Schema(description = "An error reported by the user and group or the answers storage service")
@JsonIgnoreProperties(ignoreUnknown = true)
public class JavaApiError extends ApiSubError {
  @Schema(
      description =
          "When that service produced the error, as a UTC instant with millisecond precision",
      example = "2026-10-06T11:34:04.071Z")
  private LocalDateTime timestamp;

  @Schema(example = "The IDMGroup could not be found in database.")
  private String message;

  @Schema(description = "The status that service answered with", example = "NOT_FOUND")
  private HttpStatus status;

  @Schema(
      description = "Reasons that service gave for the failure",
      example = "[The requested resource was not found.]")
  private List<String> errors;

  @Schema(example = "/user-and-group/api/v1/groups/1000")
  private String path;

  @JsonProperty("entity_error_detail")
  private EntityErrorDetail entityErrorDetail;

  @JsonCreator
  private JavaApiError(@JsonProperty("message") String message) {
    this.message = message;
  }

  /**
   * Builds an error carrying the given status, message, list of reasons, and request path, with the
   * timestamp set to the current time
   */
  public static JavaApiError of(
      HttpStatus httpStatus, String message, List<String> errors, String path) {
    JavaApiError apiError = new JavaApiError(message);
    apiError.setStatus(httpStatus);
    apiError.setTimestamp(LocalDateTime.now(Clock.systemUTC()));
    apiError.setErrors(errors);
    apiError.setPath(path);
    return apiError;
  }

  /**
   * Builds an error carrying the given status, message, single reason, and request path, with the
   * timestamp set to the current time
   */
  public static JavaApiError of(HttpStatus httpStatus, String message, String error, String path) {
    JavaApiError apiError = new JavaApiError(message);
    apiError.setStatus(httpStatus);
    apiError.setTimestamp(LocalDateTime.now(Clock.systemUTC()));
    apiError.setError(error);
    apiError.setPath(path);
    return apiError;
  }

  public static JavaApiError of(HttpStatus httpStatus, String message, List<String> errors) {
    return JavaApiError.of(httpStatus, message, errors, "");
  }

  public static JavaApiError of(HttpStatus httpStatus, String message, String error) {
    return JavaApiError.of(httpStatus, message, error, "");
  }

  public static JavaApiError of(HttpStatus httpStatus, String message) {
    return JavaApiError.of(httpStatus, message, "", "");
  }

  public static JavaApiError of(String message) {
    return JavaApiError.of(null, message, "", "");
  }

  public EntityErrorDetail getEntityErrorDetail() {
    return entityErrorDetail;
  }

  public void setEntityErrorDetail(EntityErrorDetail entityErrorDetail) {
    this.entityErrorDetail = entityErrorDetail;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  /** Returns the error message, or a placeholder when none was set */
  @Override
  public String getMessage() {
    return message == null ? "No specific message provided." : message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public void setStatus(HttpStatus status) {
    this.status = status;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(final List<String> errors) {
    this.errors = errors;
  }

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
        + getStatus()
        + ", message='"
        + message
        + '\''
        + ", errors="
        + errors
        + ", path='"
        + path
        + '\''
        + ", entityErrorDetail="
        + entityErrorDetail
        + '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, getStatus(), message, errors, path);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof JavaApiError)) return false;
    JavaApiError other = (JavaApiError) obj;
    return Objects.equals(errors, other.getErrors())
        && Objects.equals(message, other.getMessage())
        && Objects.equals(path, other.getPath())
        && Objects.equals(getStatus(), other.getStatus())
        && Objects.equals(timestamp, other.getTimestamp());
  }
}
