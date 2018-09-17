package cz.muni.csirt.kypo.elasticsearch.service.audit.impl;

import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.elasticsearch.data.dao.AuditDAO;
import cz.muni.csirt.kypo.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;
import cz.muni.csirt.kypo.elasticsearch.service.audit.AuditService;
import cz.muni.csirt.kypo.elasticsearch.service.audit.exceptions.ElasticsearchTrainingServiceLayerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

/**
 * This class have to be extended when some event should be saved to Elasticsearch.
 * 
 * @author Pavel Šeda
 *
 */
@Service
public class AuditServiceImpl<T extends AbstractAuditPOJO> implements AuditService<T> {

  private AuditDAO<T> auditDAO;

  @Autowired
  public AuditServiceImpl(AuditDAO<T> auditDAO) {
    this.auditDAO = auditDAO;
  }

  @Override
  public void save(T pojoClass) {
    Objects.requireNonNull(pojoClass, "Null class could not be saved via audit method.");
    try {
      pojoClass.setTimestamp(System.currentTimeMillis());
      pojoClass.setType(pojoClass.getClass().getName());

      auditDAO.save(pojoClass);

    } catch (IOException | ElasticsearchTrainingDataLayerException ex) {
      throw new ElasticsearchTrainingServiceLayerException(ex);
    }
  }

}
