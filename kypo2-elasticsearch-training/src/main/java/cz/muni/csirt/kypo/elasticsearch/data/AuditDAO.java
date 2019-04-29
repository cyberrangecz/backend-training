package cz.muni.csirt.kypo.elasticsearch.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;
import cz.muni.csirt.kypo.events.trainings.HintTaken;
import cz.muni.csirt.kypo.events.trainings.TrainingRunStarted;
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
public class AuditDAO extends AbstractElasticClientDAO {

    @Autowired
    public AuditDAO(RestHighLevelClient restHighLevelClient, ObjectMapper objectMapper) {
        super(restHighLevelClient, objectMapper);
    }

    /**
     * Method for saving general class into Elasticsearch under specific index and type. Index is
     * derived from package and class name lower case, and type is the same expect the class name is
     * in it's origin
     *
     * @param pojoClass class saving to Elasticsearch
     * @throws IOException
     * @throws ElasticsearchTrainingDataLayerException
     */
    public <T extends AbstractAuditPOJO> void saveTrainingRunEvent(T pojoClass, Long trainingDefinitionID, Long trainingInstanceId) throws IOException {
        String index = createIndexForTrainingRunEvents(pojoClass, trainingDefinitionID, trainingInstanceId);
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.index(index);
        indexRequest.type("default");
        indexRequest.source(getMapper().writeValueAsString(pojoClass), XContentType.JSON);
        getClient().index(indexRequest, RequestOptions.DEFAULT);
    }

    private <T extends AbstractAuditPOJO> String createIndexForTrainingRunEvents(T pojoClass, Long trainingDefinitionID, Long trainingInstanceID) {
        String packageNamePlusClassName = pojoClass.getClass().toString().toLowerCase();
        String index = "kypo3." + packageNamePlusClassName + "_evt" + "%definition=" + trainingDefinitionID + "%instance=" + trainingInstanceID;
        return index;
    }

    public ObjectMapper getMapper() {
        return super.getMapper();
    }

}
