package cz.muni.csirt.kypo.elasticsearch.service.audit.impl;

import java.io.IOException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.elasticsearch.data.dao.AuditDAO;
import cz.muni.csirt.kypo.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;
import cz.muni.csirt.kypo.elasticsearch.service.audit.AuditService;
import cz.muni.csirt.kypo.elasticsearch.service.exceptions.ElasticsearchTrainingServiceLayerException;

/**
 *
 * @author Pavel Šeda
 *
 */
@Service
public class AuditServiceImpl implements AuditService {

		private AuditDAO auditDAO;

		@Autowired
		public AuditServiceImpl(AuditDAO auditDAO) {
				this.auditDAO = auditDAO;
		}

		@Override
		public <T extends AbstractAuditPOJO> void save(T pojoClass) {
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

