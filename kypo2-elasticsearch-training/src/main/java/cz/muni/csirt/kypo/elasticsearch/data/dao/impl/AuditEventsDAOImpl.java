package cz.muni.csirt.kypo.elasticsearch.data.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.csirt.kypo.elasticsearch.data.dao.AbstractElasticClientDAO;
import cz.muni.csirt.kypo.elasticsearch.data.dao.AuditEventsDAO;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.io.IOException;

/**
 * 
 * @author Pavel Šeda
 *
 */
@Repository
public class AuditEventsDAOImpl extends AbstractElasticClientDAO implements AuditEventsDAO {

  @Autowired
  public AuditEventsDAOImpl(RestHighLevelClient client, RestClient lowLevelClient, @Qualifier("objMapperESClient") ObjectMapper mapper) {
    super(client, lowLevelClient, mapper);
  }

  @Override
  public void save(String type, String payload) throws IOException {
    IndexRequest indexRequest = new IndexRequest(new StringBuilder("kypo2-").append(type.toLowerCase()).toString(), type);
    indexRequest.source(payload, XContentType.JSON);
    getClient().index(indexRequest);
  }


  @Override
  public void update(String type, String payload) throws IOException {
    UpdateRequest updateRequest = new UpdateRequest();
    updateRequest.index("kypo2-" + type.toLowerCase());
    updateRequest.type(type);
    updateRequest.doc(payload, XContentType.JSON);
    // send update request to Elastic
    getClient().update(updateRequest);
  }

}
