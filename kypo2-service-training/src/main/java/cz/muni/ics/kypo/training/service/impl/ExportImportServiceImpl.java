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

    @Autowired
    public ExportImportServiceImpl(TrainingDefinitionRepository trainingDefinitionRepository, AbstractLevelRepository abstractLevelRepository) {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.abstractLevelRepository = abstractLevelRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public List<TrainingDefinition> findAllTrainingDefinitions() {
        return trainingDefinitionRepository.findAll();
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public List<AbstractLevel> findAllLevels() {
        return abstractLevelRepository.findAll();
    }

}
