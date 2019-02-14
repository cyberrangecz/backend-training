package cz.muni.csirt.kypo.elasticsearch.service.audit.impl;

import java.io.IOException;
import java.util.Objects;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.elasticsearch.data.dao.AuditDAO;
import cz.muni.csirt.kypo.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;
import cz.muni.csirt.kypo.elasticsearch.service.audit.AuditService;
import cz.muni.csirt.kypo.elasticsearch.service.exceptions.ElasticsearchTrainingServiceLayerException;

/**
 * @author Pavel Šeda
 */
@Service
public class AuditServiceImpl implements AuditService {

    private static Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    private ObjectMapper objectMapper;
    private AuditDAO auditDAO;

    @Autowired
    public AuditServiceImpl(AuditDAO auditDAO, @Qualifier("objMapperForElasticsearch") ObjectMapper objectMapper) {
        this.auditDAO = auditDAO;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T extends AbstractAuditPOJO> void save(T pojoClass) {
        Objects.requireNonNull(pojoClass, "Null class could not be saved via audit method.");
        try {
            pojoClass.setTimestamp(System.currentTimeMillis());
            pojoClass.setType(pojoClass.getClass().getName());

            logger.info(objectMapper.writeValueAsString(pojoClass));

            auditDAO.save(pojoClass);
        } catch (IOException | ElasticsearchTrainingDataLayerException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

}
