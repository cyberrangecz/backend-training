package cz.muni.ics.kypo.training.service.impl;

import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.ExportImportService;
import cz.muni.ics.kypo.training.utils.AssessmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
    public TrainingDefinition findById(Long trainingDefinitionId) {
        return trainingDefinitionRepository.findById(trainingDefinitionId).orElseThrow(
                () -> new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class, "id", trainingDefinitionId.getClass(),
                        trainingDefinitionId, "Training definition not found.")));
    }

    @Override
    public List<AbstractLevel> findAllLevelsFromDefinition(Long trainingDefinitionId) {
        Assert.notNull(trainingDefinitionId, "Definition id must not be null");
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingDefinitionId);
    }

    @Override
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
    public TrainingInstance findInstanceById(Long trainingInstanceId) {
        return trainingInstanceRepository.findById(trainingInstanceId).orElseThrow(
                () -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstanceId.getClass(),
                        trainingInstanceId, "Training instance not found.")));
    }

    @Override
    public Set<TrainingRun> findRunsByInstanceId(Long trainingInstanceId) {
        return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId);
    }


}
