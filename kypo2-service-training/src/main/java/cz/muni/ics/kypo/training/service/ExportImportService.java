package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.utils.AssessmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

@Service
public class ExportImportService {

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
    public ExportImportService(TrainingDefinitionRepository trainingDefinitionRepository, AbstractLevelRepository abstractLevelRepository,
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

    /**
     * Finds training definition with given id.
     *
     * @param trainingDefinitionId the id of definition to be found.
     * @return the {@link TrainingDefinition} with the given id.
     * @throws EntityNotFoundException if training definition was not found.
     */
    public TrainingDefinition findById(Long trainingDefinitionId) {
        return trainingDefinitionRepository.findById(trainingDefinitionId).orElseThrow(
                () -> new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class, "id", trainingDefinitionId.getClass(),
                        trainingDefinitionId, "Training definition not found.")));
    }

    /**
     * Find all levels associated with training definition with given id.
     *
     * @param trainingDefinitionId the id of definition which levels are to be found.
     * @return the list of all {@link AbstractLevel} that are associated with the {@link TrainingDefinition}.
     */
    public List<AbstractLevel> findAllLevelsFromDefinition(Long trainingDefinitionId) {
        Assert.notNull(trainingDefinitionId, "Definition id must not be null");
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingDefinitionId);
    }

    /**
     * Creates a level and connects it with training definition.
     *
     * @param level      the {@link AbstractLevel} to be created.
     * @param definition the {@link TrainingDefinition} to associate level with.
     */
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

    /**
     * Finds training instance with given id.
     *
     * @param trainingInstanceId the id of instance to be found.
     * @return the {@link TrainingInstance} with the given id.
     * @throws EntityNotFoundException if training instance was not found.
     */
    public TrainingInstance findInstanceById(Long trainingInstanceId) {
        return trainingInstanceRepository.findById(trainingInstanceId).orElseThrow(
                () -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstanceId.getClass(),
                        trainingInstanceId, "Training instance not found.")));
    }

    /**
     * Finds training runs associated with training instance with given id.
     *
     * @param trainingInstanceId the id of instance which runs are to be found.
     * @return the set off all {@link TrainingRun}
     */
    public Set<TrainingRun> findRunsByInstanceId(Long trainingInstanceId) {
        return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId);
    }


}
