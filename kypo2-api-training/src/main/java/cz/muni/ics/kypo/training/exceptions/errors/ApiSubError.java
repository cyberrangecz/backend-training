package cz.muni.ics.kypo.training.exceptions.errors;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.http.HttpStatus;
@ApiModel(value = "ApiSubError", subTypes = {JavaApiError.class, PythonApiError.class},
        description = "Superclass for classes JavaApiError and PythonApiError")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JavaApiError.class, name = "JavaApiError"),
        @JsonSubTypes.Type(value = PythonApiError.class, name = "PythonApiError")})
public abstract class ApiSubError {
    @ApiModelProperty(value = "The HTTP response status code", example = "404 Not found (different for each type of exception).")
    private HttpStatus status;

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
