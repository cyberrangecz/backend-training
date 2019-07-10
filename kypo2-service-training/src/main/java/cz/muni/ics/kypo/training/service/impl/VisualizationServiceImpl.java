package cz.muni.ics.kypo.training.service.impl;

import cz.muni.ics.kypo.training.annotations.security.IsAdminOrOrganizerOrTrainee;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.AbstractLevelRepository;
import cz.muni.ics.kypo.training.service.VisualizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author Dominik Pilar (445537)
 */
@Service
public class VisualizationServiceImpl implements VisualizationService {

    private static final Logger LOG = LoggerFactory.getLogger(VisualizationServiceImpl.class);
    private static final String MUST_NOT_BE_NULL = "Input training run id must not be null.";

    private AbstractLevelRepository abstractLevelRepository;
    private SecurityService securityService;

    @Autowired
    public VisualizationServiceImpl(AbstractLevelRepository abstractLevelRepository,
                                    SecurityService securityService) {
        this.abstractLevelRepository = abstractLevelRepository;
        this.securityService = securityService;
    }

    @Override
    @IsAdminOrOrganizerOrTrainee
    public List<AbstractLevel> getLevelsForVisualization(TrainingRun trainingRun) {
        Assert.notNull(trainingRun, "Id of training definition must not be null.");
        if(securityService.isAdmin()) {
            return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
        } else if(securityService.isOrganizer()) {
            if(!securityService.isOrganizerOfGivenTrainingInstance(trainingRun.getTrainingInstance().getId()) &&
            !securityService.isTraineeOfGivenTrainingRun(trainingRun.getId())) {
                throw new ServiceLayerException("Logged in user with role organizer is not organizer for training instance with id; " + trainingRun.getTrainingInstance().getId()  +
                        " of given training run with id: " + trainingRun.getId(), ErrorCode.SECURITY_RIGHTS);
            } else if(securityService.isTraineeOfGivenTrainingRun(trainingRun.getId()) && !trainingRun.getState().equals(TRState.RUNNING)) {
                throw new ServiceLayerException("Logged in user with role trainee cannot access info for visualization because training run " +
                        "with id: " + trainingRun.getId() + " is still running.", ErrorCode.RESOURCE_CONFLICT);
            } else {
                return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
            }
        } else if(securityService.isTraineeOfGivenTrainingRun(trainingRun.getId()) && trainingRun.getState().equals(TRState.RUNNING)) {
            throw new ServiceLayerException("Logged in user with role trainee cannot access info for visualization because training run " +
                    "with id: " + trainingRun.getId() + " is still running.", ErrorCode.RESOURCE_CONFLICT);
        } else if(!securityService.isTraineeOfGivenTrainingRun(trainingRun.getId())) {
                throw new ServiceLayerException("Logged in user with role organizer is not organizer for training instance with id; " + trainingRun.getTrainingInstance().getId()  +
                        " of given training run with id: " + trainingRun.getId(), ErrorCode.SECURITY_RIGHTS);
        }
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
    }
}
