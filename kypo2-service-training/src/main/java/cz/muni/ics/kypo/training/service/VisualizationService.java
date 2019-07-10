package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;

import java.util.List;

/**
 * The interface for Visualization service.
 *
 * @author Dominik Pilar (445537)
 */
public interface VisualizationService {

    /**
     * Gets list of all levels of given Training Run.
     *
     * @param trainingRun the training run for which to find all levels.
     * @return List of {@link AbstractLevel}s
     * @throws ServiceLayerException with ErrorCode: SECURITY_RIGHTS logged in user has either role organizer but is not in
     * the list of organizers of training instance of the given training run or has only role trainee but it is not his training run.
     *                                               RESOURCE_CONFLICT logged in user is trainee of given training run but training run is still running.
     */
    List<AbstractLevel> getLevelsForVisualization(TrainingRun trainingRun);

}
