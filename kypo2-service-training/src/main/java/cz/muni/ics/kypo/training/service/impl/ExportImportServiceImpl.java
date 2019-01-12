package cz.muni.ics.kypo.training.service.impl;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.ExportImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Pavel Seda
 */
@Service
public class ExportImportServiceImpl implements ExportImportService {

    private TrainingDefinitionRepository trainingDefinitionRepository;
    private AbstractLevelRepository abstractLevelRepository;
    private AssessmentLevelRepository assessmentLevelRepository;
    private InfoLevelRepository infoLevelRepository;
    private GameLevelRepository gameLevelRepository;

    private static final String LEVEL_NOT_FOUND = "Level not found.";

    @Autowired
    public ExportImportServiceImpl(TrainingDefinitionRepository trainingDefinitionRepository, AbstractLevelRepository abstractLevelRepository,
        AssessmentLevelRepository assessmentLevelRepository, InfoLevelRepository infoLevelRepository, GameLevelRepository gameLevelRepository) {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.abstractLevelRepository = abstractLevelRepository;
        this.assessmentLevelRepository = assessmentLevelRepository;
        this.gameLevelRepository = gameLevelRepository;
        this.infoLevelRepository = infoLevelRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
    public TrainingDefinition findById(Long trainingDefinitionId) {
        return trainingDefinitionRepository.findById(trainingDefinitionId).orElseThrow(
                () -> new ServiceLayerException("Training definition with id: " + trainingDefinitionId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
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
    @PreAuthorize("hasAuthority({'ADMINISTRATOR'}) or hasAuthority({T(cz.muni.ics.kypo.training.persistence.model.enums.RoleType).DESIGNER})")
    public Long createLevel(AbstractLevel level) {
        Assert.notNull(level, "Input Level cannot be null");
        if (level instanceof AssessmentLevel) {
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

}
