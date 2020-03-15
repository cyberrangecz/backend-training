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

/**
 * The type Training events service.
 */
@Service
public class TrainingEventsService {

    private TrainingEventsDAO trainingEventsDAO;

    /**
     * Instantiates a new Training events service.
     *
     * @param trainingEventsDAO the training events dao
     */
    @Autowired
    public TrainingEventsService(TrainingEventsDAO trainingEventsDAO) {
        this.trainingEventsDAO = trainingEventsDAO;
    }

    /**
     * Find all events by training definition and training instance id list.
     *
     * @param trainingDefinitionId the training definition id
     * @param trainingInstanceId   the training instance id
     * @return the list
     * @throws ElasticsearchTrainingServiceLayerException the elasticsearch training service layer exception
     */
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

    /**
     * Find all events from training run list.
     *
     * @param trainingDefinitionId the training definition id
     * @param trainingInstanceId   the training instance id
     * @param trainingRunId        the training run id
     * @return the list
     * @throws ElasticsearchTrainingServiceLayerException the elasticsearch training service layer exception
     */
    public List<Map<String, Object>> findAllEventsFromTrainingRun(Long trainingDefinitionId, Long trainingInstanceId, Long trainingRunId) throws ElasticsearchTrainingServiceLayerException {
        try {
            List<Map<String, Object>> eventsFromElasticsearch = trainingEventsDAO.findAllEventsFromTrainingRun(trainingDefinitionId, trainingInstanceId, trainingRunId);
            Collections.sort(eventsFromElasticsearch, (map1, map2) -> Long.valueOf(map1.get("timestamp").toString()).compareTo(Long.valueOf(map2.get("timestamp").toString())));
            return eventsFromElasticsearch;
        } catch (ElasticsearchTrainingDataLayerException | IOException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

    /**
     * Delete events by training instance id.
     *
     * @param instanceId the instance id
     * @throws ElasticsearchTrainingServiceLayerException the elasticsearch training service layer exception
     */
    public void deleteEventsByTrainingInstanceId(Long instanceId) throws ElasticsearchTrainingServiceLayerException {
        try {
            trainingEventsDAO.deleteEventsByTrainingInstanceId(instanceId);
        } catch (ElasticsearchTrainingDataLayerException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

    /**
     * Delete events from training run.
     *
     * @param trainingInstanceId the training instance id
     * @param trainingRunId      the training run id
     * @throws ElasticsearchTrainingServiceLayerException the elasticsearch training service layer exception
     */
    public void deleteEventsFromTrainingRun(Long trainingInstanceId, Long trainingRunId) throws ElasticsearchTrainingServiceLayerException {
        try {
            trainingEventsDAO.deleteEventsFromTrainingRun(trainingInstanceId, trainingRunId);
        } catch (ElasticsearchTrainingDataLayerException ex) {
            throw new ElasticsearchTrainingServiceLayerException(ex);
        }
    }

}
