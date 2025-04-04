package cz.cyberrange.platform.training.elasticsearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.events.AbstractAuditPOJO;
import cz.cyberrange.platform.training.elasticsearch.service.exceptions.ElasticsearchTrainingServiceLayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.springframework.util.Assert.notNull;

/**
 * The type Audit service.
 */
@Service
public class AuditService {

    private static Logger logger = LoggerFactory.getLogger(AuditService.class);

    private ObjectMapper objectMapper;

    /**
     * Instantiates a new Audit service.
     *
     * @param objectMapper the object mapper
     */
    @Autowired
    public AuditService(@Qualifier("objMapperForElasticsearch") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Method for saving general class into Elasticsearch under specific index and type.
     *
     * @param <T>       the type parameter
     * @param pojoClass class saving to Elasticsearch
     * @throws ElasticsearchTrainingServiceLayerException the elasticsearch training service layer exception
     */
    public <T extends AbstractAuditPOJO> void saveTrainingRunEvent(T pojoClass, long timestampDelay) throws ElasticsearchTrainingServiceLayerException{
        notNull(pojoClass, "Null class could not be saved via audit method.");
        try {
            pojoClass.setTimestamp(System.currentTimeMillis() + timestampDelay);
            pojoClass.setType(pojoClass.getClass().getName());

            logger.info(objectMapper.writeValueAsString(pojoClass));
        } catch (IOException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

}
