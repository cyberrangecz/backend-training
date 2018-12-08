package cz.muni.csirt.kypo.elasticsearch.data.dao;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Šeda
 */
public interface TrainingDAO {

    /**
     * Find all events in particular game.
     *
     * @param gameId Id of particular game
     * @param from   defines the offset from the first result you want to fetch.
     * @param size   of result set
     * @return collection of events
     */
    List<Map<String, Object>> findAllEventsInGame(String gameId, int from, int size) throws IOException;

}
