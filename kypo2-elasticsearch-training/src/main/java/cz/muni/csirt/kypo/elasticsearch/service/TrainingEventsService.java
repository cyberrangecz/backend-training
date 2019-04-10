package cz.muni.csirt.kypo.elasticsearch.service;

import cz.muni.csirt.kypo.elasticsearch.data.TrainingEventsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Seda
 */
@Service
public class TrainingEventsService {

    private TrainingEventsDAO trainingEventsDAO;

    @Autowired
    public TrainingEventsService(TrainingEventsDAO trainingEventsDAO) {
        this.trainingEventsDAO = trainingEventsDAO;
    }

    public List<Map<String, Object>> findAllEventsByTrainingDefinitionAndTrainingInstanceId(Long trainingDefinitionId, Long trainingInstanceId) throws IOException {
        List<Map<String, Object>> eventsFromElasticsearch = trainingEventsDAO.findAllEventsByTrainingDefinitionAndTrainingInstanceId(trainingDefinitionId, trainingInstanceId);
        //sort all events by  to be able to reduce the number of
        Collections.sort(eventsFromElasticsearch, (map1, map2) -> Long.valueOf(map1.get("timestamp").toString()).compareTo(Long.valueOf(map2.get("timestamp").toString())));



        return eventsFromElasticsearch;
    }

}
