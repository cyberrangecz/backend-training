package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.visualization.VisualizationInfoDTO;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;

/**
 * The interface Visualization facade.
 *
 * @author Dominik Pilar (445537)
 */
public interface VisualizationFacade {

    /**
     * Gather all the necessary information about the training run needed to visualize the result.
     *
     * @param trainingRunId id of Training Run to gets info.
     * @return basic info about the training definition of given a training run and the necessary info about all levels from that training run.
     * @throws FacadeLayerException when training run wiht givne ID is not found or there is some security exception in service layer
     */
    VisualizationInfoDTO getVisualizationInfoAboutTrainingRun(Long trainingRunId);

    /**
     * Gather all the necessary information about the training instance needed to visualize the result.
     *
     * @param trainingInstanceId id of Training Instance to gets info.
     * @return basic info about the training definition of given a training instance and the necessary info about all levels from that training instance.
     * @throws FacadeLayerException when training instance with given ID is not found or there is some security exception in service layer
     */
    VisualizationInfoDTO getVisualizationInfoAboutTrainingInstance(Long trainingInstanceId);
}
