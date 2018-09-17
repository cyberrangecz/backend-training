package cz.muni.csirt.kypo.elasticsearch.data.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * @author Pavel Šeda
 *
 */
public abstract class AbstractElasticClientDAO {

  protected RestHighLevelClient client;
  protected RestClient lowLevelClient;
  protected ObjectMapper mapper;

  public AbstractElasticClientDAO() {}

  public AbstractElasticClientDAO(RestHighLevelClient client, RestClient lowLevelClient, ObjectMapper mapper) {
    this.client = client;
    this.lowLevelClient = lowLevelClient;
    this.mapper = mapper;
  }

  public RestHighLevelClient getClient() {
    return client;
  }

  public void setClient(RestHighLevelClient client) {
    this.client = client;
  }

  public RestClient getLowLevelClient() {
    return lowLevelClient;
  }

  public void setLowLevelClient(RestClient lowLevelClient) {
    this.lowLevelClient = lowLevelClient;
  }

  public ObjectMapper getMapper() {
    return mapper;
  }

  public void setMapper(ObjectMapper mapper) {
    this.mapper = mapper;
  }
}


