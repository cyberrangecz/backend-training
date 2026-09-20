package cz.cyberrange.platform.training.api.exceptions.errors;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A microservice's own error detail, carried on a {@link
 * cz.cyberrange.platform.training.api.exceptions.CustomWebClientException} or {@link
 * cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException}. It reaches the error
 * response body only along the latter route, the former having no handler of its own.
 */
@Schema(
    description = "The error another platform service reported, in that service's own shape",
    subTypes = {JavaApiError.class, PythonApiError.class})
@JsonSubTypes({
  @JsonSubTypes.Type(value = JavaApiError.class, name = "JavaApiError"),
  @JsonSubTypes.Type(value = PythonApiError.class, name = "PythonApiError")
})
public abstract class ApiSubError {

  /** Returns the message this sub-error carries for reporting */
  public abstract String getMessage();
}
