package cz.cyberrange.platform.training.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.training.api.exceptions.CustomWebClientException;
import cz.cyberrange.platform.training.api.exceptions.errors.JavaApiError;
import cz.cyberrange.platform.training.api.exceptions.errors.PythonApiError;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Supplies one HTTP client per external service this service talks to. Each client is pointed at
 * its service's configured address, sends and accepts JSON, forwards the caller's own bearer token,
 * and turns any failure status into an exception carrying that service's error detail. Which shape
 * of error detail is parsed is the only thing that differs between the clients.
 */
@Import(ObjectMappersConfiguration.class)
@Configuration
public class WebClientConfig {

  @Value("${sandbox-service.uri}")
  private String sandboxService;

  @Value("${user-and-group-server.uri}")
  private String userAndGroupURI;

  @Value("${answers-storage.uri}")
  private String answersStorageURI;

  private ObjectMapper objectMapper;

  @Autowired
  public WebClientConfig(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Supplies the client for the sandbox service, which reports its failures in the Python services'
   * error shape.
   *
   * @return the client calls to the sandbox service go through
   */
  @Bean
  @Qualifier("sandboxServiceWebClient")
  public WebClient sandboxServiceWebClient() {
    return WebClient.builder()
        .baseUrl(sandboxService)
        .defaultHeaders(
            headers -> {
              headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
              headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            })
        .filters(
            exchangeFilterFunctions -> {
              exchangeFilterFunctions.add(addSecurityHeader());
              exchangeFilterFunctions.add(openStackSandboxServiceExceptionHandlingFunction());
            })
        .build();
  }

  /**
   * Supplies the client for the user-and-group service, which reports its failures in the Java
   * services' error shape.
   *
   * @return the client calls to the user-and-group service go through
   */
  @Bean
  @Qualifier("userManagementServiceWebClient")
  public WebClient userManagementServiceWebClient() {
    return WebClient.builder()
        .baseUrl(userAndGroupURI)
        .defaultHeaders(
            headers -> {
              headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
              headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            })
        .filters(
            exchangeFilterFunctions -> {
              exchangeFilterFunctions.add(addSecurityHeader());
              exchangeFilterFunctions.add(javaMicroserviceExceptionHandlingFunction());
            })
        .build();
  }

  /**
   * Supplies the client for the answer storage service, which reports its failures in the Java
   * services' error shape.
   *
   * @return the client calls to the answer storage service go through
   */
  @Bean
  @Qualifier("answersStorageWebClient")
  public WebClient answersStorageWebClient() {
    return WebClient.builder()
        .baseUrl(answersStorageURI)
        .defaultHeaders(
            headers -> {
              headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
              headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            })
        .filters(
            exchangeFilterFunctions -> {
              exchangeFilterFunctions.add(addSecurityHeader());
              exchangeFilterFunctions.add(javaMicroserviceExceptionHandlingFunction());
            })
        .build();
  }

  /**
   * Attaches the calling user's own bearer token to every outgoing request, so the called service
   * sees the request as made by that user rather than by this service. The current request must be
   * authenticated by a token; there is no anonymous path.
   *
   * @return the filter that adds the authorization header
   */
  private ExchangeFilterFunction addSecurityHeader() {
    return (request, next) -> {
      JwtAuthenticationToken jwtAuthentication =
          (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      Jwt jwtToken = jwtAuthentication.getToken();
      ClientRequest filtered =
          ClientRequest.from(request)
              .header("Authorization", "Bearer " + jwtToken.getTokenValue())
              .build();
      return next.exchange(filtered);
    };
  }

  /**
   * Turns any client or server failure status into an exception carrying the failing status and the
   * error detail parsed out of the body in the Python services' shape. A successful response passes
   * through untouched.
   *
   * @return the filter that raises the exception
   */
  private ExchangeFilterFunction openStackSandboxServiceExceptionHandlingFunction() {
    return ExchangeFilterFunction.ofResponseProcessor(
        clientResponse -> {
          if (clientResponse.statusCode().is4xxClientError()
              || clientResponse.statusCode().is5xxServerError()) {
            return clientResponse
                .bodyToMono(String.class)
                .flatMap(
                    errorBody -> {
                      PythonApiError pythonApiError = obtainSuitablePythonApiError(errorBody);
                      throw new CustomWebClientException(
                          clientResponse.statusCode(), pythonApiError);
                    });
          } else {
            return Mono.just(clientResponse);
          }
        });
  }

  /**
   * Parses the error body, falling back to a detail that says so when the body is absent or blank,
   * and to one quoting the unparsed body when it cannot be read. It never fails, so a failure
   * always carries some detail.
   *
   * @param errorBody the response body of the failed call
   * @return the parsed error detail, or a stand-in describing why there is none
   */
  private PythonApiError obtainSuitablePythonApiError(String errorBody) {
    if (errorBody == null || errorBody.isBlank()) {
      return PythonApiError.of("No specific detail provided.");
    }
    try {
      return objectMapper.readValue(errorBody, PythonApiError.class);
    } catch (IOException e) {
      return PythonApiError.of("Could not obtain error detail. Error body is: " + errorBody);
    }
  }

  /**
   * Turns any client or server failure status into an exception carrying the failing status and the
   * error detail parsed out of the body in the Java services' shape. A successful response passes
   * through untouched.
   *
   * @return the filter that raises the exception
   */
  private ExchangeFilterFunction javaMicroserviceExceptionHandlingFunction() {
    return ExchangeFilterFunction.ofResponseProcessor(
        clientResponse -> {
          if (clientResponse.statusCode().is4xxClientError()
              || clientResponse.statusCode().is5xxServerError()) {
            return clientResponse
                .bodyToMono(String.class)
                .flatMap(
                    errorBody -> {
                      JavaApiError javaApiError = obtainSuitableJavaApiError(errorBody);
                      throw new CustomWebClientException(clientResponse.statusCode(), javaApiError);
                    });
          } else {
            return Mono.just(clientResponse);
          }
        });
  }

  /**
   * Parses the error body, falling back to a message that says so when the body is absent or blank,
   * and to one quoting the unparsed body when it cannot be read. It never fails, so a failure
   * always carries some message.
   *
   * @param errorBody the response body of the failed call
   * @return the parsed error detail, or a stand-in describing why there is none
   */
  private JavaApiError obtainSuitableJavaApiError(String errorBody) {
    if (errorBody == null || errorBody.isBlank()) {
      return JavaApiError.of("No specific message provided.");
    }
    try {
      return objectMapper.readValue(errorBody, JavaApiError.class);
    } catch (IOException e) {
      return JavaApiError.of("Could not obtain error message. Error body is: " + errorBody);
    }
  }
}
