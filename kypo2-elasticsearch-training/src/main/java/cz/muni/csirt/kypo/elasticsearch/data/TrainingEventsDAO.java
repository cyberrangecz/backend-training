package cz.muni.csirt.kypo.elasticsearch.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.elasticsearch.data.dto.ElasticsearchResponseDto;
import cz.muni.csirt.kypo.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;
import cz.muni.csirt.kypo.elasticsearch.data.indexpaths.AbstractKypoElasticTermQueryFields;
import cz.muni.csirt.kypo.elasticsearch.data.indexpaths.AbstractKypoIndexPath;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The type Training events dao.
 */
@Repository
public class TrainingEventsDAO extends AbstractElasticClientDAO {

    private static final int INDEX_DOCUMENTS_MAX_RETURN_NUMBER = 10_000;

    private WebClient webClient;

    /**
     * Instantiates a new Training events dao.
     *
     * @param restHighLevelClient the rest high level client
     * @param objectMapper        the object mapper
     * @param webClient           the web client
     */
    @Autowired
    public TrainingEventsDAO(RestHighLevelClient restHighLevelClient,
                             ObjectMapper objectMapper,
                             @Qualifier("elasticsearchWebClient") WebClient webClient) {
        super(restHighLevelClient, objectMapper);
        this.webClient = webClient;
    }

    /**
     * Find all events by training definition and training instance id list.
     *
     * @param trainingDefinitionId the training definition id
     * @param trainingInstanceId   the training instance id
     * @return the list
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     * @throws IOException                             the io exception
     */
    public List<Map<String, Object>> findAllEventsByTrainingDefinitionAndTrainingInstanceId(Long trainingDefinitionId, Long trainingInstanceId) throws ElasticsearchTrainingDataLayerException, IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.sort(AbstractKypoElasticTermQueryFields.KYPO_ELASTICSEARCH_TIMESTAMP, SortOrder.ASC);
        searchSourceBuilder.size(INDEX_DOCUMENTS_MAX_RETURN_NUMBER);
        searchSourceBuilder.timeout(new TimeValue(5, TimeUnit.MINUTES));

        SearchRequest searchRequest = new SearchRequest(AbstractKypoIndexPath.KYPO3_EVENTS_INDEX + ".*" + "_evt" + ".definition=" + trainingDefinitionId + ".instance=" + trainingInstanceId);
        searchRequest.source(searchSourceBuilder);

        return handleElasticsearchResponse(getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT));
    }

    /**
     * Find all events from training run list.
     *
     * @param trainingDefinitionId the training definition id
     * @param trainingInstanceId   the training instance id
     * @param trainingRunId        the training run id
     * @return the list
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     * @throws IOException                             the io exception
     */
    public List<Map<String, Object>> findAllEventsFromTrainingRun(Long trainingDefinitionId, Long trainingInstanceId, Long trainingRunId) throws ElasticsearchTrainingDataLayerException, IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery(AbstractKypoElasticTermQueryFields.KYPO_ELASTICSEARCH_TRAINING_RUN_ID, trainingRunId));
        searchSourceBuilder.sort(AbstractKypoElasticTermQueryFields.KYPO_ELASTICSEARCH_TIMESTAMP, SortOrder.ASC);
        searchSourceBuilder.size(INDEX_DOCUMENTS_MAX_RETURN_NUMBER);
        searchSourceBuilder.timeout(new TimeValue(5, TimeUnit.MINUTES));

        SearchRequest searchRequest = new SearchRequest(AbstractKypoIndexPath.KYPO3_EVENTS_INDEX + ".*" + "_evt" + ".definition=" + trainingDefinitionId + ".instance=" + trainingInstanceId);
        searchRequest.source(searchSourceBuilder);

        return handleElasticsearchResponse(getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT));
    }

    /**
     * <pre>{@code
     *  DELETE /kypo3.cz.muni.csirt.kypo.events.trainings.*.instance={instanceId}
     * }*
     * </pre>
     *
     * @param trainingInstanceId the training instance id
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     */
    public void deleteEventsByTrainingInstanceId(Long trainingInstanceId) throws ElasticsearchTrainingDataLayerException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(AbstractKypoIndexPath.KYPO3_EVENTS_INDEX + ".*" + ".instance=" + trainingInstanceId);
        try {
            AcknowledgedResponse deleteIndexResponse = getRestHighLevelClient().indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            if (!deleteIndexResponse.isAcknowledged()) {
                throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic.");
            }
        } catch (IOException e) {
            throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic.");
        }

    }

    /**
     * Delete by query is not currently supported in Elasticsearch Rest High Level Client so the
     *
     * <pre>
     * POST /kypo3.cz.muni.csirt.kypo.events.trainings.*.instance={instanceId}/_delete_by_query
     * {
     *      "query": {
     *          "match": {
     *              "training_run_id": givenId
     *          }
     *      }
     * }
     * </pre>
     *
     * @param trainingInstanceId the training instance id
     * @param trainingRunId      the training run id
     * @throws ElasticsearchTrainingDataLayerException the elasticsearch training data layer exception
     */
    public void deleteEventsFromTrainingRun(Long trainingInstanceId, Long trainingRunId) throws ElasticsearchTrainingDataLayerException {
        String objectToPost = "{\n" +
                "  \"query\": { \n" +
                "    \"match\": {\n" +
                "      \"training_run_id\": " + trainingRunId + "\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        ElasticsearchResponseDto elasticsearchResponseDto =
                webClient
                        .post()
                        .uri("/" + AbstractKypoIndexPath.KYPO3_EVENTS_INDEX + ".*" + ".instance=" + trainingInstanceId + "/_delete_by_query")
                        .body(BodyInserters.fromPublisher(Mono.just(objectToPost), String.class))
                        .retrieve()
                        .bodyToMono(ElasticsearchResponseDto.class)
                        .block();
        if (elasticsearchResponseDto != null && !elasticsearchResponseDto.isAcknowledged()) {
            throw new ElasticsearchTrainingDataLayerException("Training run events was not deleted.");
        }
    }

    private List<Map<String, Object>> handleElasticsearchResponse(SearchResponse response) throws ElasticsearchTrainingDataLayerException {
        List<Map<String, Object>> events = new ArrayList<>();
        if (response != null) {
            SearchHits responseHits = response.getHits();
            if (responseHits != null) {
                SearchHit[] results = responseHits.getHits();
                for (SearchHit hit : results) {
                    Map<String, Object> source = hit.getSourceAsMap();
                    events.add(source);
                }
                if (events.size() == 0) {
                    throw new ElasticsearchTrainingDataLayerException("There are no events in this game.");
                }
            }
        } else {
            throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic. Please, restart Elasticsearch service.");
        }
        return events;
    }


    /**
     * This method is currently not used.
     *
     * <p>
     * This method uses reflection to convert Map<String,Object> representing particular  events to AbstractAuditPojo.
     * <p>
     * This is slowing down the retrieving process but becomes more easy to manipulate with that List of events.
     * </p>
     *
     * @param trainingDefinitionId the training definition id
     * @param trainingInstanceId   the training instance id
     * @return list
     * @throws IOException the io exception
     */
    public List<AbstractAuditPOJO> findAllEventsByTrainingDefinitionAndTrainingInstanceIdObj(Long trainingDefinitionId, Long trainingInstanceId) throws IOException {
        List<AbstractAuditPOJO> events = new ArrayList<>();

        BoolQueryBuilder queryBuilder =
                QueryBuilders
                        .boolQuery()
                        .must(
                                QueryBuilders.termQuery(AbstractKypoElasticTermQueryFields.KYPO_ELASTICSEARCH_TRAINING_DEFINITION_ID, trainingDefinitionId)
                        )
                        .must(
                                QueryBuilders.termQuery(AbstractKypoElasticTermQueryFields.KYPO_ELASTICSEARCH_TRAINING_INSTANCE_ID, trainingInstanceId)
                        );

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        SearchRequest searchRequest = new SearchRequest(AbstractKypoIndexPath.KYPO3_EVENTS_INDEX + ".*");
        searchRequest.source(searchSourceBuilder);

        SearchResponse response = getRestHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);

        if (response != null) {
            SearchHits responseHits = response.getHits();
            if (responseHits != null) {
                SearchHit[] results = responseHits.getHits();
                for (SearchHit hit : results) {
                    DocumentField documentField = hit.getFields().get("type");
                    String type = documentField.getValue();
                    try {
                        Class<?> clazz = Class.forName(type);
                        Object documentInstanceObj = clazz.newInstance();
                        Object eventClass = getMapper().readValue(hit.getSourceAsString(), documentInstanceObj.getClass());

                        if (eventClass instanceof AbstractAuditPOJO) {
                            AbstractAuditPOJO abstractAuditPOJO = (AbstractAuditPOJO) eventClass;
                            events.add(abstractAuditPOJO);
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw new ElasticsearchTrainingDataLayerException("Client could not map given field to the particular event.");
                    }
                }
            }
        } else {
            throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic.");
        }
        return events;
    }

}
