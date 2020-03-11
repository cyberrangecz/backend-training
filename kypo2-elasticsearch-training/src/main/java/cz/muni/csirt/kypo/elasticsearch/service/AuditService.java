package cz.muni.csirt.kypo.elasticsearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;
import cz.muni.csirt.kypo.elasticsearch.service.exceptions.ElasticsearchTrainingServiceLayerException;
import cz.muni.csirt.kypo.events.trainings.AssessmentAnswers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;

@Service
public class AuditService {

    private static Logger logger = LoggerFactory.getLogger(AuditService.class);

    private ObjectMapper objectMapper;

    @Autowired
    public AuditService(@Qualifier("objMapperForElasticsearch") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Method for saving general class into Elasticsearch under specific index and type.
     *
     * @param pojoClass class saving to Elasticsearch
     * @throws ElasticsearchTrainingServiceLayerException
     */
    public <T extends AbstractAuditPOJO> void saveTrainingRunEvent(T pojoClass) throws ElasticsearchTrainingServiceLayerException{
        Assert.notNull(pojoClass, "Null class could not be saved via audit method.");
        try {
            pojoClass.setTimestamp(System.currentTimeMillis());
            pojoClass.setType(pojoClass.getClass().getName());

            logger.info(objectMapper.writeValueAsString(pojoClass));
        } catch (IOException | ElasticsearchTrainingDataLayerException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

}
