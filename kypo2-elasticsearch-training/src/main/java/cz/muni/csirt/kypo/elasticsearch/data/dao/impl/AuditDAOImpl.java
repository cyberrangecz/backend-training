package cz.muni.csirt.kypo.elasticsearch.data.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.elasticsearch.data.dao.AbstractElasticClientDAO;
import cz.muni.csirt.kypo.elasticsearch.data.dao.AuditDAO;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;

/**
 * @author Pavel Šeda
 */
@Repository
public class AuditDAOImpl extends AbstractElasticClientDAO implements AuditDAO {

    @Autowired
    public AuditDAOImpl(RestHighLevelClient client, ObjectMapper mapper) {
        super(client, mapper);
    }

    @Override
    public <T extends AbstractAuditPOJO> void save(T pojoClass) throws IOException {
        String type = pojoClass.getClass().getName();
        String index = type.toLowerCase();
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.index("kypo2-" + index);
        indexRequest.type(type);
        indexRequest.source(getMapper().writeValueAsString(pojoClass), XContentType.JSON);
        getClient().index(indexRequest, RequestOptions.DEFAULT);
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
        getClient().update(updateRequest, RequestOptions.DEFAULT);
    }

    public ObjectMapper getMapper() {
        return super.getMapper();
    }

}
