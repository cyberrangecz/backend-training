package cz.muni.csirt.kypo.elasticsearch.service.audit;

import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface AuditService<T extends AbstractAuditPOJO> {

  /**
   * Method for saving general class into Elasticsearch under specific index and type.
   * 
   * @param pojoClass class saving to Elasticsearch
   */
  void save(T pojoClass);

}
