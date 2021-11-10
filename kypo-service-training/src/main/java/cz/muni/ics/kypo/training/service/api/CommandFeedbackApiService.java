package cz.muni.ics.kypo.training.service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.training.api.dto.traininglevel.LevelReferenceSolutionDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.ReferenceSolutionNodeDTO;
import cz.muni.ics.kypo.training.api.enums.MistakeType;
import cz.muni.ics.kypo.training.exceptions.CustomWebClientException;
import cz.muni.ics.kypo.training.exceptions.MicroserviceApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

/**
 * The type Command Feedback Api Service.
 */
@Service
public class CommandFeedbackApiService {

    private static final Logger LOG = LoggerFactory.getLogger(CommandFeedbackApiService.class);
    private final WebClient commandFeedbackServiceWebClient;
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new CommandFeedbackApi service.
     *
     * @param commandFeedbackServiceWebClient the web client
     */
    public CommandFeedbackApiService(WebClient commandFeedbackServiceWebClient,
                                     ObjectMapper objectMapper) {
        this.commandFeedbackServiceWebClient = commandFeedbackServiceWebClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Create reference graph in the command feedback service for the given training definition.
     *
     * @param definitionId training definition id
     * @param levelReferenceSolutionDefinitions description of the reference solution of each level.
     * @throws MicroserviceApiException error with specific message when calling command feedback microservice.
     */
    public void createReferenceGraph(Long definitionId, List<LevelReferenceSolutionDTO> levelReferenceSolutionDefinitions){
        try {
            commandFeedbackServiceWebClient
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
            throw new MicroserviceApiException("Error when calling Command Feedback API to create reference graph for definition (ID: " + definitionId +").", ex);
        }
    }

    /**
     * Create trainee graph in the command feedback service for the given training run.
     *
     * @param definitionId training definition id
     * @param instanceId training instance id
     * @param runId training run id
     * @param referenceSolution description of the reference solution.
     * @throws MicroserviceApiException error with specific message when calling command feedback microservice.
     */
    public void createTraineeGraph(Long definitionId, Long instanceId, Long runId, List<LevelReferenceSolutionDTO> referenceSolution){
        try {
            commandFeedbackServiceWebClient
                    .post()
                    .uri("/graphs/training-definitions/{definitionId}/training-instances/{instanceId}/training-runs/{runId}",
                            definitionId, instanceId, runId)
                    .body(Mono.just(objectMapper.writeValueAsString(referenceSolution)), String.class)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (IOException ex) {
            throw new SecurityException("Error while parsing reference solution", ex);
        }
        catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Command Feedback API to create trainee graph for run (ID: " + runId +").", ex);
        }
    }

    /**
     * Create summary graph in the command feedback service for the given training instance.
     *
     * @param definitionId training definition id
     * @param instanceId training instance id
     * @throws MicroserviceApiException error with specific message when calling command feedback microservice.
     */
    public void createSummaryGraph(Long definitionId, Long instanceId){
        try {
            commandFeedbackServiceWebClient
                    .post()
                    .uri("/graphs/training-definitions/{definitionId}/training-instances/{instanceId}", definitionId, instanceId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Command Feedback API to create summary graph for the training instance (ID: " + instanceId + ").", ex);
        }
    }

    /**
     * Get the reference graph for the given training definition.
     *
     * @param definitionId training definition id
     * @throws MicroserviceApiException error with specific message when calling command feedback microservice.
     */
    public Object getReferenceGraph(Long definitionId){
        try {
            return commandFeedbackServiceWebClient
                    .get()
                    .uri("/graphs/training-definitions/{definitionId}", definitionId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Command Feedback API to get reference graph for definition (ID: " + definitionId +").", ex);
        }
    }

    /**
     * Get the trainee graph for the given training run.
     *
     * @param runId training run id
     * @throws MicroserviceApiException error with specific message when calling command feedback microservice.
     */
    public Object getTraineeGraph(Long runId){
        try {
            return commandFeedbackServiceWebClient
                    .get()
                    .uri("/graphs/training-runs/{runId}", runId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Command Feedback API to get trainee graph for run (ID: " + runId +").", ex);
        }
    }

    /**
     * Get the summary graph for the given training instance.
     *
     * @param instanceId training instance id
     * @throws MicroserviceApiException error with specific message when calling command feedback microservice.
     */
    public Object getSummaryGraph(Long instanceId){
        try {
            return commandFeedbackServiceWebClient
                    .get()
                    .uri("/graphs/training-instances/{instanceId}", instanceId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Command Feedback API to get summary graph for training instance (ID: " + instanceId +").", ex);
        }
    }

    /**
     * Get aggregated correct/valid commands entered during the given training runs.
     *
     * @param runIds ids of the training runs
     * @throws MicroserviceApiException error with specific message when calling command feedback microservice.
     */
    public Object getAggregatedCorrectCommands(List<Long> runIds){
        try {
            return commandFeedbackServiceWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/commands/correct")
                            .queryParam("runIds", StringUtils.collectionToDelimitedString(runIds, ","))
                            .build())
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Command Feedback API to get correct commands for training runs (IDs: " + runIds +").", ex);
        }
    }

    /**
     * Get aggregated incorrect commands of the specific mistake types entered during the given training runs.
     *
     * @param runIds ids of the training runs
     * @param mistakeTypes type of the command mistakes that should be returned
     * @throws MicroserviceApiException error with specific message when calling command feedback microservice.
     */
    public Object getAggregatedIncorrectCommands(List<Long> runIds, List<MistakeType> mistakeTypes){
        try {
            return commandFeedbackServiceWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/commands/incorrect")
                            .queryParam("runIds", StringUtils.collectionToDelimitedString(runIds, ","))
                            .queryParam("mistakeTypes", StringUtils.collectionToDelimitedString(mistakeTypes, ","))
                            .build())
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Command Feedback API to get correct commands for training runs (IDs: " + runIds +").", ex);
        }
    }

    /**
     * Get all commands entered during the given training run.
     *
     * @param runId id of the training run
     * @throws MicroserviceApiException error with specific message when calling command feedback microservice.
     */
    public Object getAllCommandsByTrainingRun(Long runId){
        try {
            return commandFeedbackServiceWebClient
                    .get()
                    .uri("/commands/training-runs/{runId}", runId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Command Feedback API to get all commands for training run (ID: " + runId +").", ex);
        }
    }

    /**
     * Delete reference graph created for the given training definition.
     *
     * @param definitionId id of the training definition
     * @throws MicroserviceApiException error with specific message when calling command feedback microservice.
     */
    public void deleteReferenceGraph(Long definitionId){
        try {
            commandFeedbackServiceWebClient
                    .delete()
                    .uri("/graphs/reference/training-definitions/{definitionId}", definitionId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            if(ex.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw new MicroserviceApiException("Error when calling Command Feedback API to delete reference graph for training definition (ID: " + definitionId +").", ex);
            }
        }
    }

    /**
     * Delete summary graph created for the given training instance.
     *
     * @param instanceId id of the training instance
     * @throws MicroserviceApiException error with specific message when calling command feedback microservice.
     */
    public void deleteSummaryGraph(Long instanceId){
        try {
            commandFeedbackServiceWebClient
                    .delete()
                    .uri("/graphs/summary/training-instances/{instanceId}", instanceId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            if(ex.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw new MicroserviceApiException("Error when calling Command Feedback API to delete summary graph for training instance (ID: " + instanceId +").", ex);
            }
        }
    }

    /**
     * Delete trainee graph created for the given training run.
     *
     * @param runId id of the training run
     * @throws MicroserviceApiException error with specific message when calling command feedback microservice.
     */
    public void deleteTraineeGraph(Long runId){
        try {
            commandFeedbackServiceWebClient
                    .delete()
                    .uri("/graphs/trainee/training-runs/{runId}", runId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            if(ex.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw new MicroserviceApiException("Error when calling Command Feedback API to delete trainee graph for training run (ID: " + runId +").", ex);
            }
        }
    }

    /**
     * Delete all graphs (trainee and summary) created for the given training instance.
     *
     * @param instanceId id of the training instance
     * @throws MicroserviceApiException error with specific message when calling command feedback microservice.
     */
    public void deleteAllGraphsByTrainingInstance(Long instanceId){
        try {
            commandFeedbackServiceWebClient
                    .delete()
                    .uri("/graphs/training-instances/{instanceId}", instanceId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (CustomWebClientException ex){
            if(ex.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw new MicroserviceApiException("Error when calling Command Feedback API to delete all graphs created for training instance (ID: " + instanceId +").", ex);
            }
        }
    }
}

