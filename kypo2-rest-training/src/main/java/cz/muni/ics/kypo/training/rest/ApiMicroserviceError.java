package cz.muni.ics.kypo.training.rest;

import cz.muni.ics.kypo.training.exceptions.errors.ApiSubError;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Objects;
@ApiModel(value = "ApiMicroserviceError", description = "A detailed error information related to the microservice.", parent = ApiError.class)
public class ApiMicroserviceError extends ApiError {

    @ApiModelProperty(value = "Detailed error from another microservice.")
    private ApiSubError apiSubError;

    private ApiMicroserviceError() {
        super();
    }

    public static ApiError of(HttpStatus httpStatus, String message, List<String> errors, String path, ApiSubError apiSubError) {
        ApiMicroserviceError apiMicroserviceError = new ApiMicroserviceError();
        apiMicroserviceError.setTimestamp(System.currentTimeMillis());
        apiMicroserviceError.setStatus(httpStatus);
        apiMicroserviceError.setMessage(message);
        apiMicroserviceError.setErrors(errors);
        apiMicroserviceError.setPath(path);
        apiMicroserviceError.setApiSubError(apiSubError);
        return apiMicroserviceError;
    }

    public static ApiError of(HttpStatus httpStatus, String message, String error, String path, ApiSubError apiSubError) {
        ApiMicroserviceError apiMicroserviceError = new ApiMicroserviceError();
        apiMicroserviceError.setTimestamp(System.currentTimeMillis());
        apiMicroserviceError.setStatus(httpStatus);
        apiMicroserviceError.setMessage(message);
        apiMicroserviceError.setError(error);
        apiMicroserviceError.setPath(path);
        apiMicroserviceError.setApiSubError(apiSubError);
        return apiMicroserviceError;
    }

    public static ApiError of(HttpStatus httpStatus, String message, List<String> errors, ApiSubError apiSubError) {
        return ApiMicroserviceError.of(httpStatus, message, errors, "", apiSubError);
    }

    public static ApiError of(HttpStatus httpStatus, String message, String error, ApiSubError apiSubError) {
        return ApiMicroserviceError.of(httpStatus, message, error, "", apiSubError);
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
        return "ApiMicroserviceError{" +
                "apiSubError=" + apiSubError +
                ", timestamp=" + getTimestamp() +
                ", status=" + getStatus() +
                ", message='" + getMessage() + '\'' +
                ", errors=" + getErrors() +
                ", path='" + getPath() + '\'' +
                '}';
    }
}
