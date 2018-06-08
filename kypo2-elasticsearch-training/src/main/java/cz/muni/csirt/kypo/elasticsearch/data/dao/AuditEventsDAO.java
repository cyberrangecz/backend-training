package cz.muni.csirt.kypo.elasticsearch.data.dao;

import java.io.IOException;

/**
 * 
 * @author Pavel Šeda
 *
 */
public interface AuditEventsDAO {

  /**
   * Save Event.
   * 
   * @param type of event e.g.: cz.muni.csirt.kypo.events.game.WrongFlagSubmitted
   * @param payload document to be saved in Elasticsearch DB
   * @throws IOException
   */
  void save(String type, String payload) throws IOException;

  /**
   * Update particular document.
   * 
   * @param type of event e.g.: cz.muni.csirt.kypo.events.game.WrongFlagSubmitted
   * @param payload document to be saved in Elasticsearch DB
   * @throws IOException
   */
  void update(String type, String payload) throws IOException;

}
