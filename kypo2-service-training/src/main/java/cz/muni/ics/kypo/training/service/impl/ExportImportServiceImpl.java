package cz.muni.ics.kypo.training.service.impl;

import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.repository.AbstractLevelRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingDefinitionRepository;
import cz.muni.ics.kypo.training.service.ExportImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Seda
 */
@Service
public class ExportImportServiceImpl implements ExportImportService {

    private TrainingDefinitionRepository trainingDefinitionRepository;
    private AbstractLevelRepository abstractLevelRepository;

    private static final String LEVEL_NOT_FOUND = "Level not found.";

    @Autowired
    public ExportImportServiceImpl(TrainingDefinitionRepository trainingDefinitionRepository, AbstractLevelRepository abstractLevelRepository) {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.abstractLevelRepository = abstractLevelRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public List<TrainingDefinition> findAll() {
        return trainingDefinitionRepository.findAll();
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

    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
    private TrainingDefinition findById(Long id) {
        return trainingDefinitionRepository.findById(id).orElseThrow(
                () -> new ServiceLayerException("Training definition with id: " + id + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

}
