package cz.muni.csirt.kypo.elasticsearch.data.dao;

import java.io.IOException;
import java.util.Map;

/**
 * 
 * @author Pavel Šeda
 *
 */
public interface EventsDAO {

  /**
   * Find all events with Elastic native search query param: e.g.
   * ~/_search?q=cz.muni.csirt.kypo.events.game.game_details.game_instance_id:5
   * 
   * @param params Map of parameters e.g. key=q AND
   *        value=cz.muni.csirt.kypo.events.game.game_details.game_instance_id:5
   * @return
   * @throws IOException
   */
  String findAllEvents(Map<String, String> params) throws IOException;

}
