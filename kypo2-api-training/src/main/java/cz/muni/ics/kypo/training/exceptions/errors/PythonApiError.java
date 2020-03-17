package cz.muni.ics.kypo.training.exceptions.errors;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;
import java.util.Objects;
@ApiModel(value = "PythonApiError", description = "A detailed error from another Python mircorservice.", parent = ApiSubError.class)
public class PythonApiError extends ApiSubError {

    @ApiModelProperty(value = "Detail message of the error.", example = "Sandbox could not be found.")
    private String detail;
    @ApiModelProperty(value = "Parameters to specify details of the error.", example = "name: sandbox" )
    private Map<String, String> parameters;

    public PythonApiError() {
    }

    public PythonApiError(String detail) {
        this.detail = detail;
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
                ", status=" + getStatus() +
                ", parameters=" + parameters +
                '}';
    }
}
