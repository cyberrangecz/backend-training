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
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLException;
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

    private ObjectMapper objectMapper;

    @Autowired
    public WebClientConfig(@Qualifier("webClientObjectMapper") ObjectMapper objectMapper) {
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
                .filters(exchangeFilterFunctions -> exchangeFilterFunctions.add(addSecurityHeader()))
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
                    exchangeFilterFunctions.add(openStackSandboxServiceExceptionHandlingFunction());
                    exchangeFilterFunctions.add(userManagementServiceExceptionHandlingFunction());
                })
                .build();
    }

    private ExchangeFilterFunction addSecurityHeader() {
        return (request, next) -> {
            OAuth2Authentication authenticatedUser = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
            OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authenticatedUser.getDetails();
            ClientRequest filtered = ClientRequest.from(request)
                    .header("Authorization", "Bearer " + details.getTokenValue())
                    .build();
            return next.exchange(filtered);
        };
    }

    private ExchangeFilterFunction openStackSandboxServiceExceptionHandlingFunction() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if(clientResponse.statusCode().is4xxClientError() || clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        if (errorBody == null || errorBody.isBlank()) {
                            throw new CustomWebClientException("Error from external microservice. No specific message provided.", clientResponse.statusCode());
                        }
                        PythonApiError pythonApiError = null;
                        try {
                            pythonApiError = objectMapper.readValue(errorBody, PythonApiError.class);
                            pythonApiError.setStatus(clientResponse.statusCode());
                        } catch (IOException e) {
                            throw new CustomWebClientException("Error from external microservice. No specific message provided.", clientResponse.statusCode());
                        }
                        throw new CustomWebClientException(pythonApiError);
                    });
            } else {
                return Mono.just(clientResponse);
            }
        });
    }

    private ExchangeFilterFunction userManagementServiceExceptionHandlingFunction() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if(clientResponse.statusCode().is4xxClientError() || clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        if (errorBody == null || errorBody.isBlank()) {
                            throw new CustomWebClientException("Error from external microservice. No specific message provided.", clientResponse.statusCode());
                        }
                        JavaApiError javaApiError = null;
                        try {
                            javaApiError = objectMapper.readValue(errorBody, JavaApiError.class);
                            javaApiError.setStatus(clientResponse.statusCode());
                        } catch (IOException e) {
                            throw new CustomWebClientException("Error from external microservice. No specific message provided.", clientResponse.statusCode());
                        }
                        throw new CustomWebClientException(javaApiError);
                    });
            } else {
                return Mono.just(clientResponse);
            }
        });
    }

}
