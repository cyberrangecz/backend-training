package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.AbstractLevelRepository;
import cz.muni.ics.kypo.training.persistence.repository.UserRefRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

/**
 * The type Visualization service.
 */
@Service
public class VisualizationService {

    private AbstractLevelRepository abstractLevelRepository;
    private UserRefRepository userRefRepository;
    private SecurityService securityService;

    /**
     * Instantiates a new Visualization service.
     *
     * @param abstractLevelRepository the abstract level repository
     * @param securityService         the security service
     * @param userRefRepository       the user ref repository
     */
    @Autowired
    public VisualizationService(AbstractLevelRepository abstractLevelRepository,
                                SecurityService securityService, UserRefRepository userRefRepository) {
        this.abstractLevelRepository = abstractLevelRepository;
        this.securityService = securityService;
        this.userRefRepository = userRefRepository;
    }

    /**
     * Gets list of all levels for trainee of given Training Run.
     *
     * @param trainingRun the training run for which to find all levels.
     * @return List of {@link AbstractLevel}s
     * @throws EntityConflictException training run is still running
     */
    public List<AbstractLevel> getLevelsForTraineeVisualization(TrainingRun trainingRun) {
        Assert.notNull(trainingRun, "Id of training run must not be null.");
        if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)) {
            return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
        } else if (trainingRun.getState().equals(TRState.RUNNING)) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRun.getId().getClass(), trainingRun.getId(),
                    "Logged in user cannot access info for visualization because training run is still running."));
        }
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
    }

    /**
     * Gets list of all levels for organizer of given Training Instance.
     *
     * @param trainingInstance the training instance for which to find all levels.
     * @return List of {@link AbstractLevel}s
     */
    public List<AbstractLevel> getLevelsForOrganizerVisualization(TrainingInstance trainingInstance) {
        Assert.notNull(trainingInstance, "Id of training instance must not be null.");
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingInstance.getTrainingDefinition().getId());
    }

    /**
     * Get all participants ref ids in given training instance.
     *
     * @param trainingInstanceId id of Training Instance to gets participants ref ids.
     * @return list of participants ref ids.
     */
    public Set<Long> getAllParticipantsRefIdsForSpecificTrainingInstance(Long trainingInstanceId) {
        return userRefRepository.findParticipantsRefByTrainingInstanceId(trainingInstanceId);
    }
}
