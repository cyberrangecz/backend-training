package cz.muni.csirt.kypo.elasticsearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.elasticsearch.data.AuditDAO;
import cz.muni.csirt.kypo.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;
import cz.muni.csirt.kypo.elasticsearch.service.exceptions.ElasticsearchTrainingServiceLayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
public class AuditService {

    private static Logger logger = LoggerFactory.getLogger(AuditService.class);

    private ObjectMapper objectMapper;
    private AuditDAO auditDAO;

    @Autowired
    public AuditService(AuditDAO auditDAO, @Qualifier("objMapperForElasticsearch") ObjectMapper objectMapper) {
        this.auditDAO = auditDAO;
        this.objectMapper = objectMapper;
    }

    /**
     * Method for saving general class into Elasticsearch under specific index and type.
     *
     * @param pojoClass class saving to Elasticsearch
     * @throws ElasticsearchTrainingServiceLayerException
     */
    public <T extends AbstractAuditPOJO> void saveTrainingRunEvent(T pojoClass, Long trainingDefinitionID, Long trainingInstanceId) {
        Objects.requireNonNull(pojoClass, "Null class could not be saved via audit method.");
        try {
            pojoClass.setTimestamp(System.currentTimeMillis());
            pojoClass.setType(pojoClass.getClass().getName());

            logger.info(objectMapper.writeValueAsString(pojoClass));
        } catch (IOException | ElasticsearchTrainingDataLayerException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

}
