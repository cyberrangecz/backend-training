package cz.muni.ics.kypo.training.exceptions.errors;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class JavaApiError extends ApiSubError {
    @ApiModelProperty(value = "The time when the exception occurred", example = "1574062900 (different for each type of exception)")
    protected long timestamp;
    @ApiModelProperty(value = "The HTTP response status code", example = "404 Not found (different for each type of exception).")
    protected HttpStatus status;
    @ApiModelProperty(value = "The specific description of the ApiError.", example = "The IDMGroup could not be found in database (different for each type of exception).")
    protected String message;
    @ApiModelProperty(value = "The list of main reasons of the ApiError.", example = "[The requested resource was not found (different for each type of exception).]")
    protected List<String> errors;
    @ApiModelProperty(value = "The requested URI path which caused error.", example = "/kypo2-rest-user-and-group/api/v1/groups/1000 (different for each type of exception).")
    protected String path;
    @ApiModelProperty(value = "Entity detail related to the error.")
    @JsonProperty("entity_error_detail")
    private EntityErrorDetail entityErrorDetail;

    private JavaApiError() {
    }

    public static JavaApiError of(HttpStatus httpStatus, String message, List<String> errors, String path) {
        JavaApiError apiError = new JavaApiError();
        apiError.setTimestamp(System.currentTimeMillis());
        apiError.setStatus(httpStatus);
        apiError.setMessage(message);
        apiError.setErrors(errors);
        apiError.setPath(path);
        return apiError;
    }

    public static JavaApiError of(HttpStatus httpStatus, String message, String error, String path) {
        JavaApiError apiError = new JavaApiError();
        apiError.setTimestamp(System.currentTimeMillis());
        apiError.setStatus(httpStatus);
        apiError.setMessage(message);
        apiError.setError(error);
        apiError.setPath(path);
        return apiError;
    }

    public static JavaApiError of(HttpStatus httpStatus, String message, List<String> errors) {
        JavaApiError apiError = new JavaApiError();
        apiError.setTimestamp(System.currentTimeMillis());
        apiError.setStatus(httpStatus);
        apiError.setMessage(message);
        apiError.setErrors(errors);
        apiError.setPath("");
        return apiError;
    }

    public static JavaApiError of(HttpStatus httpStatus, String message, String error) {
        JavaApiError apiError = new JavaApiError();
        apiError.setTimestamp(System.currentTimeMillis());
        apiError.setStatus(httpStatus);
        apiError.setMessage(message);
        apiError.setError(error);
        apiError.setPath("");
        return apiError;
    }

    public EntityErrorDetail getEntityErrorDetail() {
        return entityErrorDetail;
    }

    public void setEntityErrorDetail(EntityErrorDetail entityErrorDetail) {
        this.entityErrorDetail = entityErrorDetail;
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
        return "ApiError{" +
                "timestamp=" + timestamp +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", errors=" + errors +
                ", path='" + path + '\'' +
                ", entityErrorDetail=" + entityErrorDetail +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, status, message, errors, path);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof JavaApiError))
            return false;
        JavaApiError other = (JavaApiError) obj;
        return Objects.equals(errors, other.getErrors()) &&
                Objects.equals(message, other.getMessage()) &&
                Objects.equals(path, other.getPath()) &&
                Objects.equals(status, other.getStatus()) &&
                Objects.equals(timestamp, other.getTimestamp());
    }

}
