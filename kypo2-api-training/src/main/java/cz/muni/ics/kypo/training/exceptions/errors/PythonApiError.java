package cz.muni.ics.kypo.training.exceptions.errors;

import io.swagger.annotations.ApiModelProperty;
import java.util.Map;

public class PythonApiError extends ApiSubError {

    @ApiModelProperty(value = "Detail message of the error.", example = "Sandbox could not be found.")
    protected String detail;
    @ApiModelProperty(value = "Parameters to specify details of the error.", example = "name: sandbox" )
    protected Map<String, String> parameters;

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
}
