package cz.cyberrange.platform.training.api.exceptions.errors;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.swagger.annotations.ApiModel;

/**
 * A microservice's own error detail, carried on a {@link
 * cz.cyberrange.platform.training.api.exceptions.CustomWebClientException} or {@link
 * cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException}. It reaches the error
 * response body only along the latter route, the former having no handler of its own.
 */
@ApiModel(
    value = "ApiSubError",
    subTypes = {JavaApiError.class, PythonApiError.class},
    description = "Superclass for classes JavaApiError and PythonApiError")
@JsonSubTypes({
  @JsonSubTypes.Type(value = JavaApiError.class, name = "JavaApiError"),
  @JsonSubTypes.Type(value = PythonApiError.class, name = "PythonApiError")
})
public abstract class ApiSubError {

  /** Returns the message this sub-error carries for reporting. */
  public abstract String getMessage();
}
