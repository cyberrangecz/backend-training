package cz.muni.csirt.kypo.elasticsearch.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import org.elasticsearch.ElasticsearchCorruptionException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
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
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Pavel Seda & Simon Hasak
 */
@Repository
public class TrainingEventsDAO extends AbstractElasticClientDAO {

    private static final int INDEX_DOCUMENTS_MAX_RETURN_NUMBER = 10_000;

    @Autowired
    public TrainingEventsDAO(RestHighLevelClient restHighLevelClient, ObjectMapper objectMapper) {
        super(restHighLevelClient, objectMapper);
    }

    public List<Map<String, Object>> findAllEventsByTrainingDefinitionAndTrainingInstanceId(Long trainingDefinitionId, Long trainingInstanceId) throws IOException {
        List<Map<String, Object>> events = new ArrayList<>();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // returns all documents under given index
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // events are sorted based on timestamp attribute
        searchSourceBuilder.sort("timestamp", SortOrder.ASC);
        searchSourceBuilder.size(INDEX_DOCUMENTS_MAX_RETURN_NUMBER);
        searchSourceBuilder.timeout(new TimeValue(5, TimeUnit.MINUTES));

        SearchRequest searchRequest = new SearchRequest("kypo3.cz.muni.csirt.kypo.events.trainings.*" + "_evt" + "%definition=" + trainingDefinitionId + "%instance=" + trainingInstanceId);
        searchRequest.source(searchSourceBuilder);

        SearchResponse response = getClient().search(searchRequest, RequestOptions.DEFAULT);
        if (response != null) {
            SearchHits responseHits = response.getHits();
            if (responseHits != null) {
                SearchHit[] results = responseHits.getHits();
                for (SearchHit hit : results) {
                    Map<String, Object> source = hit.getSourceAsMap();
                    events.add(source);
                }
                if (events.size() == 0) {
                    throw new ElasticsearchCorruptionException("There are no events in this game.");
                }
            }
        } else {
            throw new ElasticsearchCorruptionException("Client could not connect to Elastic.");
        }
        return events;
    }

    /**
     * <p>
     * This method uses reflection to convert Map<String,Object> representing particular  events to AbstractAuditPojo.
     * <p>
     * This is slowing down the retrieving process but becomes more easy to manipulate with that List of events.
     * </p>
     *
     * @param trainingDefinitionId
     * @param trainingInstanceId
     * @return
     * @throws IOException
     */
    public List<AbstractAuditPOJO> findAllEventsByTrainingDefinitionAndTrainingInstanceIdObj(Long trainingDefinitionId, Long trainingInstanceId) throws IOException {
        List<AbstractAuditPOJO> events = new ArrayList<>();

        BoolQueryBuilder queryBuilder =
                QueryBuilders
                        .boolQuery()
                        .must(
                                QueryBuilders.termQuery("training_definition_id", trainingDefinitionId)
                        )
                        .must(
                                QueryBuilders.termQuery("training_instance_id", trainingInstanceId)
                        );

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        SearchRequest searchRequest = new SearchRequest("kypo2-cz.muni.csirt.kypo.events.trainings.*");
        searchRequest.source(searchSourceBuilder);

        SearchResponse response = getClient().search(searchRequest, RequestOptions.DEFAULT);

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
                        throw new ElasticsearchCorruptionException("Client could not map given field to the particular event.");
                    }
                }
            }
        } else {
            throw new ElasticsearchCorruptionException("Client could not connect to Elastic.");
        }
        return events;
    }


}
