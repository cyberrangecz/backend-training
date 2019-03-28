package cz.muni.csirt.kypo.elasticsearch.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.ElasticsearchCorruptionException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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

    @Autowired
    public TrainingEventsDAO(RestHighLevelClient restHighLevelClient, ObjectMapper objectMapper) {
        super(restHighLevelClient, objectMapper);
    }

    public List<Map<String, Object>> findAllEventsByTrainingRunId(String trainingRunId) throws IOException {
        List<Map<String, Object>> events = new ArrayList<>();

        TermQueryBuilder queryBuilder = QueryBuilders.termQuery("training_run_id", trainingRunId);

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

}
