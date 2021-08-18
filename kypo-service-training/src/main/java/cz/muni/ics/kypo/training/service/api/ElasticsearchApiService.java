package cz.muni.ics.kypo.training.service.api;

import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.ics.kypo.training.exceptions.CustomWebClientException;
import cz.muni.ics.kypo.training.exceptions.MicroserviceApiException;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * The type User service.
 */
@Service
public class ElasticsearchApiService {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchApiService.class);
    private WebClient elasticsearchServiceWebClient;

    /**
     * Instantiates a new ElasticSearchApi service.
     *
     * @param elasticsearchServiceWebClient the web client
     */
    public ElasticsearchApiService(@Qualifier("elasticsearchServiceWebClient") WebClient elasticsearchServiceWebClient) {
        this.elasticsearchServiceWebClient = elasticsearchServiceWebClient;
    }

    /**
     * Deletes events from elasticsearch for particular training instance
     *
     * @param trainingInstanceId id of the training instance whose events to delete.
     * @throws MicroserviceApiException error with specific message when calling elasticsearch microservice.
     */
    public void deleteEventsByTrainingInstanceId(Long trainingInstanceId){
        try {
            elasticsearchServiceWebClient
                    .delete()
                    .uri("/training-platform-events/training-instances/{instanceId}", trainingInstanceId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API to delete events for particular instance (ID: "+ trainingInstanceId +").", ex);
        }
    }


    /**
     * Obtain events from elasticsearch for particular training run
     *
     * @param trainingRun thee training run whose events to obtain.
     * @throws MicroserviceApiException error with specific message when calling elasticsearch microservice.
     */
    public List<Map<String, Object>> findAllEventsFromTrainingRun(TrainingRun trainingRun){
        try {
            Long definitionId = trainingRun.getTrainingInstance().getTrainingDefinition().getId();
            Long instanceId = trainingRun.getTrainingInstance().getId();
            return elasticsearchServiceWebClient
                    .get()
                    .uri("/training-platform-events/training-definitions/{definitionId}/training-instances/{instanceId}/training-runs/{runId}", definitionId, instanceId, trainingRun.getId())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular run (ID: "+ trainingRun.getId() +").", ex);
        }
    }

    /**
     * Obtain events from elasticsearch for particular training instance.
     *
     * @param trainingInstance the training instance whose events to obtain.
     * @return Aggregated events by user and levels.
     * @throws MicroserviceApiException error with specific message when calling elasticsearch microservice.
     */
    public Map<Long, Map<Long, List<AbstractAuditPOJO>>> getAggregatedEventsByLevelsAndTrainingRuns(TrainingInstance trainingInstance) {
        try {
            Long instanceId = trainingInstance.getId();
            return elasticsearchServiceWebClient
                    .get()
                    .uri("/training-platform-events/training-instances/{instanceId}/aggregated/levels/training-runs", instanceId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<Long, Map<Long, List<AbstractAuditPOJO>>>>() {})
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular instance (ID: "+ trainingInstance.getId() +").", ex);
        }
    }

    /**
     * Obtain events from elasticsearch for particular training instance.
     *
     * @param trainingInstance the training instance whose events to obtain.
     * @return Aggregated events by user and levels.
     * @throws MicroserviceApiException error with specific message when calling elasticsearch microservice.
     */
    public Map<Long, Map<Long, List<AbstractAuditPOJO>>> getAggregatedEventsByTrainingRunsAndLevels(TrainingInstance trainingInstance) {
        try {
            Long instanceId = trainingInstance.getId();
            return elasticsearchServiceWebClient
                    .get()
                    .uri("/training-platform-events/training-instances/{instanceId}/aggregated/training-runs/levels", instanceId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<Long, Map<Long, List<AbstractAuditPOJO>>>>() {})
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular instance (ID: "+ trainingInstance.getId() +").", ex);
        }
    }

    /**
     * Deletes events from elasticsearch for particular training run
     *
     * @param trainingInstanceId id of the training instance in which the training run is running.
     * @param trainingRunId id of the training run whose events to delete.
     * @throws MicroserviceApiException error with specific message when calling elasticsearch microservice.
     */
    public void deleteEventsFromTrainingRun(Long trainingInstanceId, Long trainingRunId){
        try {
            elasticsearchServiceWebClient
                    .delete()
                    .uri("/training-platform-events/training-instances/{instanceId}/training-runs/{runId}", trainingInstanceId, trainingRunId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API to delete events for particular training run (ID: "+ trainingRunId +").", ex);
        }
    }

    public List<Map<String, Object>> findAllConsoleCommandsFromSandbox(Long sandboxId){
        try {
            return elasticsearchServiceWebClient
                    .get()
                    .uri("/training-platform-commands/sandboxes/{sandboxId}", sandboxId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        }catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular sandbox (ID: "+ sandboxId +").", ex);
        }
    }

    public List<Map<String, Object>> findAllConsoleCommandsFromSandboxAndTimeRange(Integer sandboxId, Long from, Long to){
        try {
            return elasticsearchServiceWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/training-platform-commands/sandboxes/{sandboxId}/ranges")
                            .queryParam("from", from)
                            .queryParam("to", to)
                            .build(sandboxId)
                    )
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        }catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular commands of sandbox (ID: "+ sandboxId +").", ex);
        }
    }

    public void deleteBashCommandsFromPool(Long poolId){
        try{
            elasticsearchServiceWebClient
                    .delete()
                    .uri("/training-platform-commands/pools/{poolIdfind}", poolId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        }catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API to delete bash commands for particular pool (ID: "+ poolId +").", ex);
        }
    }
}

