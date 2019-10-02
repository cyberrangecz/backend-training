package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.VisualizationInfoDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

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

    /**
     * Gather all the necessary information about the users for specific training instance.
     *
     * @param trainingInstanceId id of Training Instance to gets info about all participants.
     * @return basic info about the participants of given a training instance.
     * @throws FacadeLayerException when training instance with given ID is not found or there is some security exception in service layer
     */
    List<UserRefDTO> getParticipantsForGivenTrainingInstance(Long trainingInstanceId);

    /**
     * Gather all the necessary information about the users with given ids.
     *
     * @param usersIds ids of the users to be retrieved.
     * @param pageable  pageable parameter with information about pagination.
     * @return basic info about the users with given ids.
     * @throws FacadeLayerException when training instance with given ID is not found or there is some security exception in service layer
     */
    PageResultResource<UserRefDTO> getUsersByIds(Set<Long> usersIds, Pageable pageable);
}
