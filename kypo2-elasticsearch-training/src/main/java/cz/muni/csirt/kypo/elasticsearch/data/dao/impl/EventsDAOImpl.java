package cz.muni.csirt.kypo.elasticsearch.data.dao.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.csirt.kypo.elasticsearch.data.dao.AbstractElasticClientDAO;
import cz.muni.csirt.kypo.elasticsearch.data.dao.EventsDAO;

/**
 * 
 * @author Pavel Šeda
 *
 */
@Repository
public class EventsDAOImpl extends AbstractElasticClientDAO implements EventsDAO {

  @Autowired
  public EventsDAOImpl(RestHighLevelClient client, RestClient lowLevelClient, @Qualifier("objMapperESClient") ObjectMapper mapper) {
    super(client, lowLevelClient, mapper);
  }

  @Override
  public String findAllEvents(Map<String, String> params) throws IOException {
    Response response = lowLevelClient.performRequest("GET", "kypo2*/_search", params);
    HttpEntity entity = response.getEntity();
    InputStream stream = entity.getContent();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
      String responseToReturn = br.lines().collect(Collectors.joining(System.lineSeparator()));
      System.out.println(responseToReturn);
      return responseToReturn;
    }
  }
}

