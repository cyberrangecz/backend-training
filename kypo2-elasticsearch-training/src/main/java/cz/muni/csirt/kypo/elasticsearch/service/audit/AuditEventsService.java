package cz.muni.csirt.kypo.elasticsearch.service.audit;

/**
 * 
 * @author Pavel Seda
 *
 */
public interface AuditEventsService {

  /**
   * Save Event.
   * 
   * @param type of event e.g.: cz.muni.csirt.kypo.events.game.WrongFlagSubmitted
   * @param payload document to be saved in Elasticsearch DB
   */
  void save(String type, String payload);

}
