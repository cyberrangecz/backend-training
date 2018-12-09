package cz.muni.csirt.kypo.elasticsearch.data.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.csirt.kypo.elasticsearch.data.QueryStrings;
import cz.muni.csirt.kypo.elasticsearch.data.dao.AbstractElasticClientDAO;
import cz.muni.csirt.kypo.elasticsearch.data.dao.TrainingDAO;
import cz.muni.csirt.kypo.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
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
 * @author Pavel Šeda
 */
@Repository
public class TrainingDAOImpl extends AbstractElasticClientDAO implements TrainingDAO {

    @Autowired
    public TrainingDAOImpl(RestHighLevelClient client, RestClient lowLevelClient, ObjectMapper mapper) {
        super(client, lowLevelClient, mapper);
    }

    @Override
    public List<Map<String, Object>> findAllEventsInGame(String gameId, int from, int size) throws IOException {
        List<Map<String, Object>> events = new ArrayList<>();
        // builds query
        QueryBuilder builder = QueryBuilders.termQuery(QueryStrings.ES_EVENT_GAME_DETAILS_GAME_INSTANCE_ID, gameId);
        // set search builder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(from);
        sourceBuilder.size(size); // max returned events
        sourceBuilder.fetchSource("*", QueryStrings.ES_EVENT_GAME_DETAILS_GAME_INSTANCE_ID);
        sourceBuilder.query(builder);
        // paging elasticsearch
        sourceBuilder.timeout(new TimeValue(80, TimeUnit.SECONDS));

        // set search request on particular index
        SearchRequest searchRequest = new SearchRequest(QueryStrings.ES_EVENTS_INDEX_WILDCHARD);
        searchRequest.source(sourceBuilder);

        // client search
        SearchResponse response = getClient().search(searchRequest);
        if (response != null) {
            SearchHits responseHits = response.getHits();
            if (responseHits != null) {
                SearchHit[] results = responseHits.getHits();
                for (SearchHit hit : results) {
                    Map<String, Object> source = hit.getSourceAsMap();
                    events.add(source);
                }
                if (events.isEmpty()) {
                    throw new ElasticsearchTrainingDataLayerException("There are no events in this game.");
                }
            }
        } else {
            throw new ElasticsearchTrainingDataLayerException("Client could not connect to Elastic.");
        }
        return events;
    }

    public ObjectMapper getMapper() {
        return super.getMapper();
    }

}
