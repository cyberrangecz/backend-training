package cz.muni.ics.kypo.training.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.training.exceptions.CustomWebClientException;
import cz.muni.ics.kypo.training.exceptions.errors.JavaApiError;
import cz.muni.ics.kypo.training.exceptions.errors.PythonApiError;
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
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * The type Web client config.
 */
@Import(ObjectMappersConfiguration.class)
@Configuration
public class WebClientConfig {


    @Value("${openstack-server.uri}")
    private String kypoOpenStackURI;
    @Value("${user-and-group-server.uri}")
    private String userAndGroupURI;
    @Value("${elasticsearch-service.uri}")
    private String elasticsearchServiceURI;
    @Value("${answers-storage.uri}")
    private String answersStorageURI;
    @Value("${training-feedback-service.uri}")
    private String trainingFeedbackServiceURI;

    private ObjectMapper objectMapper;

    @Autowired
    public WebClientConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Openstack service web client web client.
     *
     * @return the web client
     */
    @Bean
    @Qualifier("sandboxServiceWebClient")
    public WebClient sandboxServiceWebClient() {
        return WebClient.builder()
                .baseUrl(kypoOpenStackURI)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                })
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(addSecurityHeader());
                    exchangeFilterFunctions.add(openStackSandboxServiceExceptionHandlingFunction());
                })
                .build();
    }

    /**
     * User management service web client web client.
     *
     * @return the web client
     */
    @Bean
    @Qualifier("userManagementServiceWebClient")
    public WebClient userManagementServiceWebClient() {
        return WebClient.builder()
                .baseUrl(userAndGroupURI)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                })
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(addSecurityHeader());
                    exchangeFilterFunctions.add(javaMicroserviceExceptionHandlingFunction());
                })
                .build();
    }

    /**
     * Elasticsearch service web client.
     *
     * @return the web client
     */
    @Bean
    @Qualifier("elasticsearchServiceWebClient")
    public WebClient elasticsearchServiceWebClient() {
        return WebClient.builder()
                .baseUrl(elasticsearchServiceURI)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                })
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(addSecurityHeader());
                    exchangeFilterFunctions.add(javaMicroserviceExceptionHandlingFunction());
                })
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }

    /**
     * Answers storage web client.
     *
     * @return the web client
     */
    @Bean
    @Qualifier("answersStorageWebClient")
    public WebClient answersStorageWebClient() {
        return WebClient.builder()
                .baseUrl(answersStorageURI)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                })
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(addSecurityHeader());
                    exchangeFilterFunctions.add(javaMicroserviceExceptionHandlingFunction());
                })
                .build();
    }

    /**
     * Training feedback service web client.
     *
     * @return the web client
     */
    @Bean
    public WebClient trainingFeedbackServiceWebClient() {
        return WebClient.builder()
                .baseUrl(trainingFeedbackServiceURI)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                })
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(addSecurityHeader());
                    exchangeFilterFunctions.add(javaMicroserviceExceptionHandlingFunction());
                })
                .build();
    }

    private ExchangeFilterFunction addSecurityHeader() {
        return (request, next) -> {
            JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            Jwt jwtToken = jwtAuthentication.getToken();
            ClientRequest filtered = ClientRequest.from(request)
                    .header("Authorization", "Bearer " + jwtToken.getTokenValue())
                    .build();
            return next.exchange(filtered);
        };
    }

    private ExchangeFilterFunction openStackSandboxServiceExceptionHandlingFunction() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if(clientResponse.statusCode().is4xxClientError() || clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        PythonApiError pythonApiError = obtainSuitablePythonApiError(errorBody);
                        throw new CustomWebClientException(clientResponse.statusCode(), pythonApiError);

                    });
            } else {
                return Mono.just(clientResponse);
            }
        });
    }

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

    private ExchangeFilterFunction javaMicroserviceExceptionHandlingFunction() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if(clientResponse.statusCode().is4xxClientError() || clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        JavaApiError javaApiError = obtainSuitableJavaApiError(errorBody);
                        throw new CustomWebClientException(clientResponse.statusCode(), javaApiError);
                    });
            } else {
                return Mono.just(clientResponse);
            }
        });
    }

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
