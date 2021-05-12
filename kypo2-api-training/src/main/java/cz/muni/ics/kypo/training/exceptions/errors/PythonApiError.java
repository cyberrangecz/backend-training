package cz.muni.ics.kypo.training.exceptions.errors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Objects;
@ApiModel(value = "PythonApiError", description = "A detailed error from another Python mircorservice.", parent = ApiSubError.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PythonApiError extends ApiSubError {

    @ApiModelProperty(value = "Detail message of the error.", example = "Sandbox could not be found.")
    private String detail;
    @ApiModelProperty(value = "Parameters to specify details of the error.", example = "name: sandbox" )
    private Map<String, String> parameters;

    private PythonApiError() {
    }

    public static PythonApiError of(String detail) {
        PythonApiError apiError = new PythonApiError();
        apiError.setDetail(detail);
        return apiError;
    }

    public static PythonApiError of(String detail, Map<String, String> parameters ) {
        PythonApiError apiError = new PythonApiError();
        apiError.setDetail(detail);
        apiError.setParameters(parameters);
        return apiError;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String getMessage() {
        return detail == null ? "No specific message provided." : detail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PythonApiError)) return false;
        PythonApiError that = (PythonApiError) o;
        return Objects.equals(getDetail(), that.getDetail()) &&
                Objects.equals(getParameters(), that.getParameters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDetail(), getParameters());
    }

    @Override
    public String toString() {
        return "PythonApiError{" +
                "detail='" + detail + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
