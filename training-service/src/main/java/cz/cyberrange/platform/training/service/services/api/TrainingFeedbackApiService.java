package cz.cyberrange.platform.training.service.services.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.training.api.dto.traininglevel.LevelReferenceSolutionDTO;
import cz.cyberrange.platform.training.api.enums.MistakeType;
import cz.cyberrange.platform.training.api.exceptions.CustomWebClientException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.function.Function;

/**
 * The type Training Feedback Api Service.
 */
@Service
public class TrainingFeedbackApiService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingFeedbackApiService.class);
    private final WebClient trainingFeedbackServiceWebClient;
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new TrainingFeedbackApiService service.
     *
     * @param trainingFeedbackServiceWebClient the web client
     */
    public TrainingFeedbackApiService(WebClient trainingFeedbackServiceWebClient,
                                      ObjectMapper objectMapper) {
        this.trainingFeedbackServiceWebClient = trainingFeedbackServiceWebClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Create reference graph in the training feedback service for the given training definition.
     *
     * @param definitionId training definition id
     * @param levelReferenceSolutionDefinitions description of the reference solution of each level.
     * @throws MicroserviceApiException error with specific message when calling training feedback microservice.
     */
    public void createReferenceGraph(Long definitionId, List<LevelReferenceSolutionDTO> levelReferenceSolutionDefinitions){
        try {
            trainingFeedbackServiceWebClient
                    .post()
                    .uri("/graphs/training-definitions/{definitionId}", definitionId)
                    .body(Mono.just(objectMapper.writeValueAsString(levelReferenceSolutionDefinitions)), String.class)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (IOException ex) {
            throw new SecurityException("Error while parsing reference solution", ex);
        }
        catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Training Feedback API to create reference graph for definition (ID: " + definitionId +").", ex);
        }
    }

    /**
     * Create trainee graph in the training feedback service for the given training run.
     *
     * @param definitionId training definition id
     * @param instanceId training instance id
     * @param runId training run id
     * @param referenceSolution description of the reference solution.
     * @param accessToken access token of the training instance. Optional.
     * @throws MicroserviceApiException error with specific message when calling training feedback microservice.
     */
    public void createTraineeGraph(Long definitionId, Long instanceId, Long runId, List<LevelReferenceSolutionDTO> referenceSolution, String accessToken){
        try {
            Function<UriBuilder, URI> uriFunction = uriBuilder -> {
                uriBuilder = uriBuilder.path("/graphs/training-definitions/{definitionId}/training-instances/{instanceId}/training-runs/{runId}");
                if (accessToken != null) {
                    uriBuilder = uriBuilder.queryParam("accessToken", accessToken);
                }
                return uriBuilder.build(definitionId, instanceId, runId);
            };
            trainingFeedbackServiceWebClient
                    .post()
                    .uri(uriFunction)
                    .body(Mono.just(objectMapper.writeValueAsString(referenceSolution)), String.class)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (IOException ex) {
            throw new SecurityException("Error while parsing reference solution", ex);
        }
        catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Training Feedback API to create trainee graph for run (ID: " + runId +").", ex);
        }
    }

    /**
     * Create summary graph in the training feedback service for the given training instance.
     *
     * @param definitionId training definition id
     * @param instanceId training instance id
     * @throws MicroserviceApiException error with specific message when calling training feedback microservice.
     */
    public void createSummaryGraph(Long definitionId, Long instanceId){
        try {
            trainingFeedbackServiceWebClient
                    .post()
                    .uri("/graphs/training-definitions/{definitionId}/training-instances/{instanceId}", definitionId, instanceId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Training Feedback API to create summary graph for the training instance (ID: " + instanceId + ").", ex);
        }
    }

    /**
     * Get the reference graph for the given training definition.
     *
     * @param definitionId training definition id
     * @throws MicroserviceApiException error with specific message when calling training feedback microservice.
     */
    public Object getReferenceGraph(Long definitionId){
        try {
            return trainingFeedbackServiceWebClient
                    .get()
                    .uri("/graphs/training-definitions/{definitionId}", definitionId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Training Feedback API to get reference graph for definition (ID: " + definitionId +").", ex);
        }
    }

    /**
     * Get the trainee graph for the given training run.
     *
     * @param runId training run id
     * @throws MicroserviceApiException error with specific message when calling training feedback microservice.
     */
    public Object getTraineeGraph(Long runId){
        try {
            return trainingFeedbackServiceWebClient
                    .get()
                    .uri("/graphs/training-runs/{runId}", runId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Training Feedback API to get trainee graph for run (ID: " + runId +").", ex);
        }
    }

    /**
     * Get the summary graph for the given training instance.
     *
     * @param instanceId training instance id
     * @throws MicroserviceApiException error with specific message when calling training feedback microservice.
     */
    public Object getSummaryGraph(Long instanceId){
        try {
            return trainingFeedbackServiceWebClient
                    .get()
                    .uri("/graphs/training-instances/{instanceId}", instanceId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Training Feedback API to get summary graph for training instance (ID: " + instanceId +").", ex);
        }
    }

    /**
     * Get aggregated correct/valid commands entered during the given training runs.
     *
     * @param runIds ids of the training runs
     * @throws MicroserviceApiException error with specific message when calling training feedback microservice.
     */
    public Object getAggregatedCorrectCommands(List<Long> runIds){
        try {
            return trainingFeedbackServiceWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/commands/correct")
                            .queryParam("runIds", StringUtils.collectionToDelimitedString(runIds, ","))
                            .build())
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Training Feedback API to get correct commands for training runs (IDs: " + runIds +").", ex);
        }
    }

    /**
     * Get aggregated incorrect commands of the specific mistake types entered during the given training runs.
     *
     * @param runIds ids of the training runs
     * @param mistakeTypes type of the command mistakes that should be returned
     * @throws MicroserviceApiException error with specific message when calling training feedback microservice.
     */
    public Object getAggregatedIncorrectCommands(List<Long> runIds, List<MistakeType> mistakeTypes){
        try {
            return trainingFeedbackServiceWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/commands/incorrect")
                            .queryParam("runIds", StringUtils.collectionToDelimitedString(runIds, ","))
                            .queryParam("mistakeTypes", StringUtils.collectionToDelimitedString(mistakeTypes, ","))
                            .build())
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Training Feedback API to get correct commands for training runs (IDs: " + runIds +").", ex);
        }
    }

    /**
     * Get all commands entered during the given training run.
     *
     * @param runId id of the training run
     * @throws MicroserviceApiException error with specific message when calling training feedback microservice.
     */
    public Object getAllCommandsByTrainingRun(Long runId){
        try {
            return trainingFeedbackServiceWebClient
                    .get()
                    .uri("/commands/training-runs/{runId}", runId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Training Feedback API to get all commands for training run (ID: " + runId +").", ex);
        }
    }

    /**
     * Delete reference graph created for the given training definition.
     *
     * @param definitionId id of the training definition
     * @throws MicroserviceApiException error with specific message when calling training feedback microservice.
     */
    public void deleteReferenceGraph(Long definitionId){
        try {
            trainingFeedbackServiceWebClient
                    .delete()
                    .uri("/graphs/reference/training-definitions/{definitionId}", definitionId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            if(ex.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw new MicroserviceApiException("Error when calling Training Feedback API to delete reference graph for training definition (ID: " + definitionId +").", ex);
            }
        }
    }

    /**
     * Delete summary graph created for the given training instance.
     *
     * @param instanceId id of the training instance
     * @throws MicroserviceApiException error with specific message when calling training feedback microservice.
     */
    public void deleteSummaryGraph(Long instanceId){
        try {
            trainingFeedbackServiceWebClient
                    .delete()
                    .uri("/graphs/summary/training-instances/{instanceId}", instanceId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            if(ex.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw new MicroserviceApiException("Error when calling Training Feedback API to delete summary graph for training instance (ID: " + instanceId +").", ex);
            }
        }
    }

    /**
     * Delete trainee graph created for the given training run.
     *
     * @param runId id of the training run
     * @throws MicroserviceApiException error with specific message when calling training feedback microservice.
     */
    public void deleteTraineeGraph(Long runId){
        try {
            trainingFeedbackServiceWebClient
                    .delete()
                    .uri("/graphs/trainee/training-runs/{runId}", runId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            if(ex.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw new MicroserviceApiException("Error when calling Training Feedback API to delete trainee graph for training run (ID: " + runId +").", ex);
            }
        }
    }

    /**
     * Delete all graphs (trainee and summary) created for the given training instance.
     *
     * @param instanceId id of the training instance
     * @throws MicroserviceApiException error with specific message when calling training feedback microservice.
     */
    public void deleteAllGraphsByTrainingInstance(Long instanceId){
        try {
            trainingFeedbackServiceWebClient
                    .delete()
                    .uri("/graphs/training-instances/{instanceId}", instanceId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            if(ex.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw new MicroserviceApiException("Error when calling Training Feedback API to delete all graphs created for training instance (ID: " + instanceId +").", ex);
            }
        }
    }
}

