package cz.muni.csirt.kypo.elasticsearch.data.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * @author Pavel Šeda
 *
 */
public abstract class AbstractElasticClientDAO {

	private RestHighLevelClient client;
	private RestClient lowLevelClient;
	private ObjectMapper mapper;

	protected AbstractElasticClientDAO(RestHighLevelClient client, RestClient lowLevelClient, ObjectMapper mapper) {
		this.client = client;
		this.lowLevelClient = lowLevelClient;
		this.mapper = mapper;
	}

	protected RestHighLevelClient getClient() {
		return client;
	}

	protected void setClient(RestHighLevelClient client) {
		this.client = client;
	}

	protected RestClient getLowLevelClient() {
		return lowLevelClient;
	}

	protected void setLowLevelClient(RestClient lowLevelClient) {
		this.lowLevelClient = lowLevelClient;
	}

	protected ObjectMapper getMapper() {
		return mapper;
	}

	protected void setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}
}
