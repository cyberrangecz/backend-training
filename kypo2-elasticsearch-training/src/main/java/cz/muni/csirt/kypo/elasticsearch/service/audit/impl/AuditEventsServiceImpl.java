package cz.muni.csirt.kypo.elasticsearch.service.audit.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.muni.csirt.kypo.elasticsearch.data.dao.AuditEventsDAO;
import cz.muni.csirt.kypo.elasticsearch.service.audit.AuditEventsService;
import cz.muni.csirt.kypo.elasticsearch.service.audit.exceptions.ElasticsearchTrainingServiceLayerException;
import cz.muni.csirt.kypo.elasticsearch.service.eventvalidation.EventValidation;
import cz.muni.csirt.kypo.utils.MyObjectsUtility;

/**
 * 
 * @author Pavel Šeda
 *
 */
@Service
public class AuditEventsServiceImpl implements AuditEventsService {

  private AuditEventsDAO eventsDAO;
  private EventValidation eventValidation;

  @Autowired
  public AuditEventsServiceImpl(AuditEventsDAO eventsDAO, EventValidation eventValidation) {
    this.eventsDAO = eventsDAO;
    this.eventValidation = eventValidation;
  }

  @Override
  public void save(String type, String payload) {
    MyObjectsUtility.requireNonNullNonEmptyString(payload, "Added payload could not be empty.");
    try {
      if (eventValidation.isValid(type, payload)) {
        eventsDAO.save(type, payload); // retrieves type from payload
                                       // string
      } else {
        throw new ElasticsearchTrainingServiceLayerException("This event is not valid");
      }
    } catch (IOException | ElasticsearchTrainingServiceLayerException ex) {
      throw new ElasticsearchTrainingServiceLayerException(ex);
    }
  }

}
