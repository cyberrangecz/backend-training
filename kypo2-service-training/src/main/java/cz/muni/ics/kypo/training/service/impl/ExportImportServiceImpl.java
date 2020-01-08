package cz.muni.ics.kypo.training.service.impl;

import cz.muni.ics.kypo.training.annotations.security.IsAdminOrDesignerOrOrganizer;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.ExportImportService;
import cz.muni.ics.kypo.training.utils.AssessmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ExportImportServiceImpl implements ExportImportService {

    @Value("${openstack-server.uri}")
    private String kypoOpenStackURI;

    private TrainingDefinitionRepository trainingDefinitionRepository;
    private AbstractLevelRepository abstractLevelRepository;
    private AssessmentLevelRepository assessmentLevelRepository;
    private InfoLevelRepository infoLevelRepository;
    private GameLevelRepository gameLevelRepository;
    private TrainingInstanceRepository trainingInstanceRepository;
    private TrainingRunRepository trainingRunRepository;

    @Autowired
    public ExportImportServiceImpl(TrainingDefinitionRepository trainingDefinitionRepository, AbstractLevelRepository abstractLevelRepository,
                                   AssessmentLevelRepository assessmentLevelRepository, InfoLevelRepository infoLevelRepository,
                                   GameLevelRepository gameLevelRepository, TrainingInstanceRepository trainingInstanceRepository,
                                   TrainingRunRepository trainingRunRepository) {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.abstractLevelRepository = abstractLevelRepository;
        this.assessmentLevelRepository = assessmentLevelRepository;
        this.gameLevelRepository = gameLevelRepository;
        this.infoLevelRepository = infoLevelRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.trainingRunRepository = trainingRunRepository;
    }

    @Override
    @IsAdminOrDesignerOrOrganizer
    public TrainingDefinition findById(Long trainingDefinitionId) {
        return trainingDefinitionRepository.findById(trainingDefinitionId).orElseThrow(
                () -> new ServiceLayerException("Training definition with id: " + trainingDefinitionId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @IsAdminOrDesignerOrOrganizer
    public List<AbstractLevel> findAllLevelsFromDefinition(Long trainingDefinitionId) {
        Assert.notNull(trainingDefinitionId, "Definition id must not be null");
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingDefinitionId);
    }

    @Override
    @IsDesignerOrAdmin
    public void createLevel(AbstractLevel level, TrainingDefinition definition) {
        Assert.notNull(level, "Input Level cannot be null");
        Assert.notNull(definition, "Input definition cannot be null");
        level.setOrder(abstractLevelRepository.getCurrentMaxOrder(definition.getId()) + 1);
        level.setTrainingDefinition(definition);
        if (level instanceof AssessmentLevel) {
            AssessmentUtil.validQuestions(((AssessmentLevel) level).getQuestions());
            assessmentLevelRepository.save((AssessmentLevel) level);
        } else if (level instanceof InfoLevel) {
            infoLevelRepository.save((InfoLevel) level);
        } else {
            gameLevelRepository.save((GameLevel) level);
        }
    }

    @Override
    @IsOrganizerOrAdmin
    public TrainingInstance findInstanceById(Long trainingInstanceId) {
        return trainingInstanceRepository.findById(trainingInstanceId).orElseThrow(
                () -> new ServiceLayerException("Training instance with id: " + trainingInstanceId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @IsOrganizerOrAdmin
    public Set<TrainingRun> findRunsByInstanceId(Long trainingInstanceId) {
        return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId);
    }

    @Override
    @IsOrganizerOrAdmin
    public void failIfInstanceIsNotFinished(LocalDateTime endTime) {
        LocalDateTime currentTime = LocalDateTime.now(Clock.systemUTC());
        if (currentTime.isBefore(endTime))
            throw new ServiceLayerException("The training instance is not finished.", ErrorCode.RESOURCE_CONFLICT);
    }
}
