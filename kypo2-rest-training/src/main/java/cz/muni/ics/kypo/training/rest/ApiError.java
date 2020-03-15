package cz.muni.ics.kypo.training.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.muni.ics.kypo.training.exceptions.errors.ApiSubError;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The type Api error.
 */
public class ApiError {
    /**
     * The Timestamp.
     */
    @ApiModelProperty(value = "The time when the exception occurred", example = "1574062900 (different for each type of exception)")
    protected long timestamp;
    /**
     * The Status.
     */
    @ApiModelProperty(value = "The HTTP response status code", example = "404 Not found (different for each type of exception).")
    protected HttpStatus status;
    /**
     * The Message.
     */
    @ApiModelProperty(value = "The specific description of the ApiError.", example = "The IDMGroup could not be found in database (different for each type of exception).")
    protected String message;
    /**
     * The Errors.
     */
    @ApiModelProperty(value = "The list of main reasons of the ApiError.", example = "[The requested resource was not found (different for each type of exception).]")
    protected List<String> errors;
    /**
     * The Path.
     */
    @ApiModelProperty(value = "The requested URI path which caused error.", example = "/kypo2-rest-user-and-group/api/v1/groups/1000 (different for each type of exception).")
    protected String path;
    @ApiModelProperty(value = "Entity detail related to the error.")
    @JsonProperty("entity_error_detail")
    private EntityErrorDetail entityErrorDetail;
    @ApiModelProperty(value = "Detailed error from another microservice.")
    private ApiSubError apiSubError;

    private ApiError() {
    }

    /**
     * Of api error.
     *
     * @param httpStatus the http status
     * @param message    the message
     * @param errors     the errors
     * @param path       the path
     * @return the api error
     */
    public static ApiError of(HttpStatus httpStatus, String message, List<String> errors, String path) {
        ApiError apiError = new ApiError();
        apiError.setTimestamp(System.currentTimeMillis());
        apiError.setStatus(httpStatus);
        apiError.setMessage(message);
        apiError.setErrors(errors);
        apiError.setPath(path);
        return apiError;
    }

    /**
     * Of api error.
     *
     * @param httpStatus the http status
     * @param message    the message
     * @param error      the error
     * @param path       the path
     * @return the api error
     */
    public static ApiError of(HttpStatus httpStatus, String message, String error, String path) {
        ApiError apiError = new ApiError();
        apiError.setTimestamp(System.currentTimeMillis());
        apiError.setStatus(httpStatus);
        apiError.setMessage(message);
        apiError.setError(error);
        apiError.setPath(path);
        return apiError;
    }

    /**
     * Of api error.
     *
     * @param httpStatus the http status
     * @param message    the message
     * @param errors     the errors
     * @return the api error
     */
    public static ApiError of(HttpStatus httpStatus, String message, List<String> errors) {
        ApiError apiError = new ApiError();
        apiError.setTimestamp(System.currentTimeMillis());
        apiError.setStatus(httpStatus);
        apiError.setMessage(message);
        apiError.setErrors(errors);
        apiError.setPath("");
        return apiError;
    }

    /**
     * Of api error.
     *
     * @param httpStatus the http status
     * @param message    the message
     * @param error      the error
     * @return the api error
     */
    public static ApiError of(HttpStatus httpStatus, String message, String error) {
        ApiError apiError = new ApiError();
        apiError.setTimestamp(System.currentTimeMillis());
        apiError.setStatus(httpStatus);
        apiError.setMessage(message);
        apiError.setError(error);
        apiError.setPath("");
        return apiError;
    }

    /**
     * Gets entity error detail.
     *
     * @return the entity error detail
     */
    public EntityErrorDetail getEntityErrorDetail() {
        return entityErrorDetail;
    }

    /**
     * Sets entity error detail.
     *
     * @param entityErrorDetail the entity error detail
     */
    public void setEntityErrorDetail(EntityErrorDetail entityErrorDetail) {
        this.entityErrorDetail = entityErrorDetail;
    }

    /**
     * Gets api sub error.
     *
     * @return the api sub error
     */
    public ApiSubError getApiSubError() {
        return apiSubError;
    }

    /**
     * Sets api sub error.
     *
     * @param apiSubError the api sub error
     */
    public void setApiSubError(ApiSubError apiSubError) {
        this.apiSubError = apiSubError;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets timestamp.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(final HttpStatus status) {
        this.status = status;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Gets errors.
     *
     * @return the errors
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Sets errors.
     *
     * @param errors the errors
     */
    public void setErrors(final List<String> errors) {
        this.errors = errors;
    }

    /**
     * Sets error.
     *
     * @param error the error
     */
    public void setError(final String error) {
        errors = Arrays.asList(error);
    }

    /**
     * Gets path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets path.
     *
     * @param path the path
     */
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
                ", apiSubError=" + apiSubError +
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
        if (!(obj instanceof ApiError))
            return false;
        ApiError other = (ApiError) obj;
        return Objects.equals(errors, other.getErrors()) &&
                Objects.equals(message, other.getMessage()) &&
                Objects.equals(path, other.getPath()) &&
                Objects.equals(status, other.getStatus()) &&
                Objects.equals(timestamp, other.getTimestamp());
    }

}
