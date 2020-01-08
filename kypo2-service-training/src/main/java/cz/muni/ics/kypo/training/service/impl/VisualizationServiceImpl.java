package cz.muni.ics.kypo.training.service.impl;

import cz.muni.ics.kypo.training.annotations.security.IsAdminOrTrainee;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.AbstractLevelRepository;
import cz.muni.ics.kypo.training.persistence.repository.UserRefRepository;
import cz.muni.ics.kypo.training.service.VisualizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

@Service
public class VisualizationServiceImpl implements VisualizationService {

    private static final Logger LOG = LoggerFactory.getLogger(VisualizationServiceImpl.class);
    private static final String MUST_NOT_BE_NULL = "Input training run id must not be null.";

    private AbstractLevelRepository abstractLevelRepository;
    private UserRefRepository userRefRepository;
    private SecurityService securityService;

    @Autowired
    public VisualizationServiceImpl(AbstractLevelRepository abstractLevelRepository,
                                    SecurityService securityService,  UserRefRepository userRefRepository) {
        this.abstractLevelRepository = abstractLevelRepository;
        this.securityService = securityService;
        this.userRefRepository = userRefRepository;
    }

    @Override
    @IsAdminOrTrainee
    public List<AbstractLevel> getLevelsForTraineeVisualization(TrainingRun trainingRun) {
        Assert.notNull(trainingRun, "Id of training run must not be null.");
        if(securityService.isAdmin()) {
            return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
        } else if(!securityService.isTraineeOfGivenTrainingRun(trainingRun.getId())) {
            throw new ServiceLayerException("Logged in user is not trainee of given training run with id: " + trainingRun.getId()  +
                    ".", ErrorCode.SECURITY_RIGHTS);
        } else if(securityService.isTraineeOfGivenTrainingRun(trainingRun.getId()) && trainingRun.getState().equals(TRState.RUNNING)) {
            throw new ServiceLayerException("Logged in user cannot access info for visualization because training run " +
                    "with id: " + trainingRun.getId() + " is still running.", ErrorCode.RESOURCE_CONFLICT);
        }
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
    }

    @Override
    @IsOrganizerOrAdmin
    public List<AbstractLevel> getLevelsForOrganizerVisualization(TrainingInstance trainingInstance) {
        Assert.notNull(trainingInstance, "Id of training instance must not be null.");
        if(securityService.isAdmin()) {
            return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingInstance.getTrainingDefinition().getId());
        } else if(!securityService.isOrganizerOfGivenTrainingInstance(trainingInstance.getId())) {
                throw new ServiceLayerException("Logged in user is not organizer for training instance with id; " + trainingInstance.getId()  +
                        ".", ErrorCode.SECURITY_RIGHTS);
        }
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingInstance.getTrainingDefinition().getId());
    }

    @Override
    public Set<Long> getAllParticipantsRefIdsForSpecificTrainingInstance(Long trainingInstanceId) {
        return userRefRepository.findParticipantsRefByTrainingInstanceId(trainingInstanceId);
    }
}
