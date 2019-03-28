package cz.muni.csirt.kypo.elasticsearch.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * @author Pavel Šeda
 */
public abstract class AbstractElasticClientDAO {

    private RestHighLevelClient client;
    private ObjectMapper mapper;

    protected AbstractElasticClientDAO(RestHighLevelClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    protected RestHighLevelClient getClient() {
        return client;
    }

    protected void setClient(RestHighLevelClient client) {
        this.client = client;
    }

    protected ObjectMapper getMapper() {
        return mapper;
    }

    protected void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }
}
