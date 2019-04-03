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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Pavel Seda
 */
@Service
public class ExportImportServiceImpl implements ExportImportService {

    @Value("${openstack-server.uri}")
    private String kypoOpenStackURI;

    private static final String LEVEL_NOT_FOUND = "Level not found.";

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
    public List<AbstractLevel> findAllLevelsFromDefinition(Long id) {
        Assert.notNull(id, "Definition id must not be null");
        TrainingDefinition trainingDefinition = findById(id);
        List<AbstractLevel> levels = new ArrayList<>();
        Long levelId = trainingDefinition.getStartingLevel();
        AbstractLevel level = null;
        while (levelId != null) {
            level = abstractLevelRepository.findById(levelId)
                    .orElseThrow(() -> new ServiceLayerException(LEVEL_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
            levels.add(level);
            levelId = level.getNextLevel();
        }
        return levels;
    }

    @Override
    @IsDesignerOrAdmin
    public Long createLevel(AbstractLevel level) {
        Assert.notNull(level, "Input Level cannot be null");
        if (level instanceof AssessmentLevel) {
            AssessmentUtil.validQuestions(((AssessmentLevel) level).getQuestions());
            AbstractLevel newLevel = assessmentLevelRepository.save((AssessmentLevel) level);
            return newLevel.getId();
        } else if (level instanceof InfoLevel) {
            AbstractLevel newLevel = infoLevelRepository.save((InfoLevel) level);
            return newLevel.getId();
        } else {
            AbstractLevel newLevel = gameLevelRepository.save((GameLevel) level);
            return newLevel.getId();
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
    public void failIfInstanceIsNotFinished(LocalDateTime endTime) {
        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isBefore(endTime))
            throw new ServiceLayerException("The training instance is not finished.", ErrorCode.RESOURCE_CONFLICT);
    }
}
