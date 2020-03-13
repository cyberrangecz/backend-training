package cz.muni.csirt.kypo.elasticsearch.service;

import cz.muni.csirt.kypo.elasticsearch.data.TrainingEventsDAO;
import cz.muni.csirt.kypo.elasticsearch.data.exceptions.ElasticsearchTrainingDataLayerException;
import cz.muni.csirt.kypo.elasticsearch.service.exceptions.ElasticsearchTrainingServiceLayerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class TrainingEventsService {

    private TrainingEventsDAO trainingEventsDAO;

    @Autowired
    public TrainingEventsService(TrainingEventsDAO trainingEventsDAO) {
        this.trainingEventsDAO = trainingEventsDAO;
    }

    public List<Map<String, Object>> findAllEventsByTrainingDefinitionAndTrainingInstanceId(Long trainingDefinitionId, Long trainingInstanceId) throws ElasticsearchTrainingServiceLayerException {
        try {
            List<Map<String, Object>> eventsFromElasticsearch = trainingEventsDAO.findAllEventsByTrainingDefinitionAndTrainingInstanceId(trainingDefinitionId, trainingInstanceId);
            Collections.sort(eventsFromElasticsearch, (map1, map2) -> Long.valueOf(map1.get("timestamp").toString()).compareTo(Long.valueOf(map2.get("timestamp").toString())));
            Collections.sort(eventsFromElasticsearch, (map1, map2) -> Long.valueOf(map1.get("timestamp").toString()).compareTo(Long.valueOf(map2.get("timestamp").toString())));
            return eventsFromElasticsearch;
        } catch (ElasticsearchTrainingDataLayerException | IOException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

    public List<Map<String, Object>> findAllEventsFromTrainingRun(Long trainingDefinitionId, Long trainingInstanceId, Long trainingRunId) throws ElasticsearchTrainingServiceLayerException {
        try {
            List<Map<String, Object>> eventsFromElasticsearch = trainingEventsDAO.findAllEventsFromTrainingRun(trainingDefinitionId, trainingInstanceId, trainingRunId);
            Collections.sort(eventsFromElasticsearch, (map1, map2) -> Long.valueOf(map1.get("timestamp").toString()).compareTo(Long.valueOf(map2.get("timestamp").toString())));
            return eventsFromElasticsearch;
        } catch (ElasticsearchTrainingDataLayerException | IOException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

    public void deleteEventsByTrainingInstanceId(Long instanceId) throws ElasticsearchTrainingServiceLayerException {
        try {
            trainingEventsDAO.deleteEventsByTrainingInstanceId(instanceId);
        } catch (ElasticsearchTrainingDataLayerException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

    public void deleteEventsFromTrainingRun(Long trainingInstanceId, Long trainingRunId) throws ElasticsearchTrainingServiceLayerException {
        try {
            trainingEventsDAO.deleteEventsFromTrainingRun(trainingInstanceId, trainingRunId);
        } catch (ElasticsearchTrainingDataLayerException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

}
