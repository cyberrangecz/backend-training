package cz.muni.csirt.kypo.elasticsearch.service;

import cz.muni.csirt.kypo.elasticsearch.data.TrainingEventsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Seda & Simon Hasak
 */
@Service
public class TrainingEventsService {

    private TrainingEventsDAO trainingEventsDAO;

    @Autowired
    public TrainingEventsService(TrainingEventsDAO trainingEventsDAO) {
        this.trainingEventsDAO = trainingEventsDAO;
    }

    public List<Map<String, Object>> findAllEventsByTrainingRunId(String trainingRunId) throws IOException {
        var result = trainingEventsDAO.findAllEventsByTrainingRunId(trainingRunId);
        System.out.println(result.toArray().length);
        return result;
    }

}
