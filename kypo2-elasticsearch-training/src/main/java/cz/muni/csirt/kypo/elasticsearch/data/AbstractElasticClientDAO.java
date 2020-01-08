package cz.muni.csirt.kypo.elasticsearch.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.RestHighLevelClient;

public abstract class AbstractElasticClientDAO {

    private RestHighLevelClient restHighLevelClient;
    private ObjectMapper mapper;

    protected AbstractElasticClientDAO(RestHighLevelClient client, ObjectMapper mapper) {
        this.restHighLevelClient = client;
        this.mapper = mapper;
    }

    protected RestHighLevelClient getRestHighLevelClient() {
        return restHighLevelClient;
    }

    protected void setRestHighLevelClient(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    protected ObjectMapper getMapper() {
        return mapper;
    }

    protected void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

}
