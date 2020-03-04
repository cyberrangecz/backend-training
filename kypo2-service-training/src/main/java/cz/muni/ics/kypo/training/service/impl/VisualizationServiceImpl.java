package cz.muni.ics.kypo.training.service.impl;

import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.AbstractLevelRepository;
import cz.muni.ics.kypo.training.persistence.repository.UserRefRepository;
import cz.muni.ics.kypo.training.service.VisualizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

@Service
public class VisualizationServiceImpl implements VisualizationService {

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
    public List<AbstractLevel> getLevelsForTraineeVisualization(TrainingRun trainingRun) {
        Assert.notNull(trainingRun, "Id of training run must not be null.");
        if(securityService.isAdmin()) {
            return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
        } else if(trainingRun.getState().equals(TRState.RUNNING)) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRun.getId().getClass(), trainingRun.getId(),
                    "Logged in user cannot access info for visualization because training run is still running."));
        }
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
    }

    @Override
    public List<AbstractLevel> getLevelsForOrganizerVisualization(TrainingInstance trainingInstance) {
        Assert.notNull(trainingInstance, "Id of training instance must not be null.");
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingInstance.getTrainingDefinition().getId());
    }

    @Override
    public Set<Long> getAllParticipantsRefIdsForSpecificTrainingInstance(Long trainingInstanceId) {
        return userRefRepository.findParticipantsRefByTrainingInstanceId(trainingInstanceId);
    }
}
