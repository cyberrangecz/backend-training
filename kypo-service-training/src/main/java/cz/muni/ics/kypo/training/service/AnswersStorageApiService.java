package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.responses.SandboxAnswersInfo;
import cz.muni.ics.kypo.training.exceptions.CustomWebClientException;
import cz.muni.ics.kypo.training.exceptions.MicroserviceApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * The type Answers Storage Api Service.
 */
@Service
public class AnswersStorageApiService {

    private static final Logger LOG = LoggerFactory.getLogger(AnswersStorageApiService.class);
    private final WebClient answersStorageWebClient;

    /**
     * Instantiates a new AnswersStorageApi service.
     *
     * @param answersStorageWebClient the web client
     */
    public AnswersStorageApiService(WebClient answersStorageWebClient) {
        this.answersStorageWebClient = answersStorageWebClient;
    }

    /**
     * Get correct answer for the given sandbox and with specific answer identifier.
     *
     * @param sandboxId id of the sandbox used by trainee.
     * @param answerVariableName identifier of the answer.
     * @throws MicroserviceApiException error with specific message when calling answer storage microservice.
     */
    public String getCorrectAnswerBySandboxIdAndVariableName(Long sandboxId, String answerVariableName){
        try {
            return answersStorageWebClient
                    .get()
                    .uri("/sandboxes/{sandboxId}/answers/{answerVariableName}", sandboxId, answerVariableName)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Answers Storage API to get correct answer (Identifier: "+ answerVariableName +") for sandbox (ID: " + sandboxId + ").", ex);
        }
    }


    /**
     * Get all answers generated for the given sandbox.
     *
     * @param sandboxId id of the sandbox.
     * @throws MicroserviceApiException error with specific message when calling answer storage microservice.
     */
    public SandboxAnswersInfo getAnswersBySandboxId(Long sandboxId) {
        try {
            return answersStorageWebClient
                    .get()
                    .uri("/sandboxes/{sandboxId}/answers", sandboxId)
                    .retrieve()
                    .bodyToMono(SandboxAnswersInfo.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Answers Storage API to get correct answers for sandbox (ID: " + sandboxId + ").", ex);
        }
    }

    /**
     * Get all answers generated for the given sandboxes.
     *
     * @param sandboxIds ids of the sandboxes.
     * @throws MicroserviceApiException error with specific message when calling answers storage microservice.
     */
    public PageResultResource<SandboxAnswersInfo> getAnswersBySandboxIds(List<Long> sandboxIds) {
        try {
            return answersStorageWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sandboxes")
                            .queryParam("id", sandboxIds)
                            .queryParam("page", 0)
                            .queryParam("size", Integer.MAX_VALUE)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PageResultResource<SandboxAnswersInfo>>() {})
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Answers Storage API to get correct answers for sandboxes (IDs: " + sandboxIds + ").", ex);
        }
    }
}

