package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;

import java.util.List;
import java.util.Set;

/**
 * The interface for Visualization service.
 *
 * @author Dominik Pilar (445537)
 */
public interface VisualizationService {

    /**
     * Gets list of all levels for trainee of given Training Run.
     *
     * @param trainingRun the training run for which to find all levels.
     * @return List of {@link AbstractLevel}s
     * @throws ServiceLayerException with ErrorCode: SECURITY_RIGHTS logged in user is not trainee of given training instance.
     *                                               RESOURCE_CONFLICT training run is still running.
     */
    List<AbstractLevel> getLevelsForTraineeVisualization(TrainingRun trainingRun);

    /**
     * Gets list of all levels for organizer of given Training Instance.
     *
     * @param trainingInstance the training instance for which to find all levels.
     * @return List of {@link AbstractLevel}s
     * @throws ServiceLayerException with ErrorCode: SECURITY_RIGHTS logged in user is not organizer of given training instance.
     */
    List<AbstractLevel> getLevelsForOrganizerVisualization(TrainingInstance trainingInstance);

    /**
     * Get all participants ref ids in given training instance.
     *
     * @param trainingInstanceId id of Training Instance to gets participants ref ids.
     * @return list of participants ref ids.
     */
    Set<Long> getAllParticipantsRefIdsForSpecificTrainingInstance(Long trainingInstanceId);

}
