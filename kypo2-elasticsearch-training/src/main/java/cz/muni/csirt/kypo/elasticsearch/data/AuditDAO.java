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
        String packageName = pojoClass.getClass().getPackageName().toLowerCase();
        String className = pojoClass.getClass().getSimpleName().toLowerCase();
        String index = "kypo2-" + packageName + ".definition-" + trainingDefinitionID + ".instance-" + trainingInstanceID + "." + className + "_evt";
        return index;
    }

    /**
     * Update particular document.
     *
     * @param pojoClass class updating in Elasticsearch
     * @throws IOException
     * @throws ElasticsearchTrainingDataLayerException
     */
    public <T extends AbstractAuditPOJO> void update(T pojoClass) throws IOException {
        String type = pojoClass.getClass().getName();
        String index = type.toLowerCase();
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index("kypo2-" + index);
        updateRequest.doc(getMapper().writeValueAsString(pojoClass), XContentType.JSON);
        // send update request to Elastic
        getClient().update(updateRequest, RequestOptions.DEFAULT);
    }

    public ObjectMapper getMapper() {
        return super.getMapper();
    }


    public static void main(String[] args) {
        TrainingRunStarted trainingRunStarted = TrainingRunStarted.builder()
                .sandboxId(1)
                .trainingDefinitionId(1)
                .trainingInstanceId(1)
                .trainingRunId(1)
                .playerLogin("test")
                .totalScore(50)
                .actualScoreInLevel(50)
                .level(1)
                .build();

        String packageName = trainingRunStarted.getClass().getPackageName().toLowerCase();
        String className = trainingRunStarted.getClass().getSimpleName().toLowerCase();
        String index = "kypo2-" + packageName + ".definition-" + 18 + ".instance-" + 8 + "." + className + "_evt";
        System.out.println(index);
    }

}
