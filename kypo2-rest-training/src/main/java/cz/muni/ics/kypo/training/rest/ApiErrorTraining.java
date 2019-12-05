package cz.muni.ics.kypo.training.rest;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Pavel Seda (441048)
 */
public class ApiErrorTraining {
    @ApiModelProperty(value = "The time when the exception occurred", example = "1574062900 (different for each type of exception)")
    private long timestamp;
    @ApiModelProperty(value = "The HTTP response status code", example = "404 Not found (different for each type of exception).")
    private HttpStatus status;
    @ApiModelProperty(value = "The specific description of the ApiError.", example = "The IDMGroup could not be found in database (different for each type of exception).")
    private String message;
    @ApiModelProperty(value = "The list of main reasons of the ApiError.", example = "[The requested resource was not found (different for each type of exception).]")
    private List<String> errors;
    @ApiModelProperty(value = "The requested URI path which caused error.", example = "/kypo2-rest-user-and-group/api/v1/groups/1000 (different for each type of exception).")
    private String path;

    public static class ApiErrorBuilder {

        private final HttpStatus status;
        private final String message;
        private List<String> errors = new ArrayList<>();
        private String path = "";

        public ApiErrorBuilder(HttpStatus status, String message) {
            Objects.requireNonNull(status, "HttpStatus is necessary to not be null.");
            Objects.requireNonNull(message, "It is required to provide error message.");
            this.status = status;
            this.message = message;
        }

        public ApiErrorBuilder setError(String error) {
            Objects.requireNonNull(error, "Given error could not be null");
            this.errors = Arrays.asList(error);
            return this;
        }

        public ApiErrorBuilder setErrors(List<String> errors) {
            Objects.requireNonNull(errors, "Given list of errors could not be null");
            this.errors = errors;
            return this;
        }

        public ApiErrorBuilder setPath(String path) {
            Objects.requireNonNull(path, "Given path could not be null");
            this.path = path;
            return this;
        }

        public ApiErrorTraining build() {
            return new ApiErrorTraining(this);
        }
    }

    private ApiErrorTraining(ApiErrorBuilder builder) {
        super();
        this.timestamp = System.currentTimeMillis();
        this.status = builder.status;
        this.message = builder.message;
        this.errors = builder.errors;
        this.path = builder.path;
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
        return "APIError [timestamp=" + timestamp + ", status=" + status + ", message=" + message + ", errors=" + errors + ", path=" + path
                + "]";
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
        if (!(obj instanceof ApiErrorTraining))
            return false;
        ApiErrorTraining other = (ApiErrorTraining) obj;
        return Objects.equals(errors, other.getErrors()) &&
                Objects.equals(message, other.getMessage()) &&
                Objects.equals(path, other.getPath()) &&
                Objects.equals(status, other.getStatus()) &&
                Objects.equals(timestamp, other.getTimestamp());
    }

}
