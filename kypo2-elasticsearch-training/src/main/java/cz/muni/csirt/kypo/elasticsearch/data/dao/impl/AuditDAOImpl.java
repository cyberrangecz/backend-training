package cz.muni.csirt.kypo.elasticsearch.data.dao.impl;

import java.io.IOException;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.elasticsearch.data.dao.AbstractElasticClientDAO;
import cz.muni.csirt.kypo.elasticsearch.data.dao.AuditDAO;

/**
 * 
 * @author Pavel Šeda
 *
 */
@Repository
public class AuditDAOImpl extends AbstractElasticClientDAO implements AuditDAO {

	@Autowired
	public AuditDAOImpl(RestHighLevelClient client, RestClient lowLevelClient,
			@Qualifier("objMapperESClient") ObjectMapper mapper) {
		super(client, lowLevelClient, mapper);
	}

	@Override
	public <T extends AbstractAuditPOJO> void save(T pojoClass) throws IOException {
		String type = pojoClass.getClass().getName();
		String index = type.toLowerCase();
		IndexRequest indexRequest = new IndexRequest("kypo2-" + index, type);
		indexRequest.source(getMapper().writeValueAsString(pojoClass), XContentType.JSON);
		getClient().index(indexRequest);
	}

	@Override
	public <T extends AbstractAuditPOJO> void update(T pojoClass) throws IOException {
		String type = pojoClass.getClass().getName();
		String index = type.toLowerCase();
		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.index("kypo2-" + index);
		updateRequest.type(type);
		updateRequest.doc(getMapper().writeValueAsString(pojoClass), XContentType.JSON);
		// send update request to Elastic
		getClient().update(updateRequest);
	}

	public ObjectMapper getMapper() {
		return super.getMapper();
	}

}
