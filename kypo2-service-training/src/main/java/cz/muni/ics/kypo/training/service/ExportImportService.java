package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.api.responses.SandboxDefinitionInfo;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.exceptions.errors.PythonApiError;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.utils.AssessmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

/**
 * The type Export import service.
 */
@Service
public class ExportImportService {

    private TrainingDefinitionRepository trainingDefinitionRepository;
    private AbstractLevelRepository abstractLevelRepository;
    private AssessmentLevelRepository assessmentLevelRepository;
    private InfoLevelRepository infoLevelRepository;
    private GameLevelRepository gameLevelRepository;
    private TrainingInstanceRepository trainingInstanceRepository;
    private TrainingRunRepository trainingRunRepository;
    private RestTemplate pythonRestTemplate;

    /**
     * Instantiates a new Export import service.
     *
     * @param trainingDefinitionRepository the training definition repository
     * @param abstractLevelRepository      the abstract level repository
     * @param assessmentLevelRepository    the assessment level repository
     * @param infoLevelRepository          the info level repository
     * @param gameLevelRepository          the game level repository
     * @param trainingInstanceRepository   the training instance repository
     * @param trainingRunRepository        the training run repository
     * @param pythonRestTemplate           the python rest template
     */
    @Autowired
    public ExportImportService(TrainingDefinitionRepository trainingDefinitionRepository,
                               AbstractLevelRepository abstractLevelRepository,
                               AssessmentLevelRepository assessmentLevelRepository,
                               InfoLevelRepository infoLevelRepository,
                               GameLevelRepository gameLevelRepository,
                               TrainingInstanceRepository trainingInstanceRepository,
                               TrainingRunRepository trainingRunRepository,
                               @Qualifier("pythonRestTemplate") RestTemplate pythonRestTemplate) {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.abstractLevelRepository = abstractLevelRepository;
        this.assessmentLevelRepository = assessmentLevelRepository;
        this.gameLevelRepository = gameLevelRepository;
        this.infoLevelRepository = infoLevelRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.trainingRunRepository = trainingRunRepository;
        this.pythonRestTemplate = pythonRestTemplate;
    }

    /**
     * Finds training definition with given id.
     *
     * @param trainingDefinitionId the id of definition to be found.
     * @return the {@link TrainingDefinition} with the given id.
     * @throws EntityNotFoundException if training definition was not found.
     */
    public TrainingDefinition findById(Long trainingDefinitionId) {
        return trainingDefinitionRepository.findById(trainingDefinitionId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class, "id", trainingDefinitionId.getClass(),
                        trainingDefinitionId, "Training definition not found.")));
    }

    /**
     * Creates a level and connects it with training definition.
     *
     * @param level      the {@link AbstractLevel} to be created.
     * @param definition the {@link TrainingDefinition} to associate level with.
     */
    public void createLevel(AbstractLevel level, TrainingDefinition definition) {
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
        return trainingInstanceRepository.findById(trainingInstanceId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstanceId.getClass(),
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

    /**
     * Gets sandbox definition id.
     *
     * @param poolId the pool id
     * @return the sandbox definition id
     */
    public SandboxDefinitionInfo getSandboxDefinitionId(Long poolId) {
        try {
            return pythonRestTemplate.getForObject("/pools/{poolId}/definition", SandboxDefinitionInfo.class, Long.toString(poolId));
        } catch (CustomRestTemplateException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ForbiddenException("There is no available sandbox definition for particular pool (ID: " + poolId + ").");
            }
            throw new MicroserviceApiException("Error when calling Python API to sandbox for particular pool (ID: " + poolId + ")", new PythonApiError(ex.getMessage()));
        }
    }

}
