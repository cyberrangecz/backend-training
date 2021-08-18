package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.api.responses.SandboxDefinitionInfo;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.question.QuestionAnswer;
import cz.muni.ics.kypo.training.persistence.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Export import service.
 */
@Service
public class ExportImportService {

    private final TrainingDefinitionRepository trainingDefinitionRepository;
    private final AbstractLevelRepository abstractLevelRepository;
    private final AssessmentLevelRepository assessmentLevelRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final InfoLevelRepository infoLevelRepository;
    private final TrainingLevelRepository trainingLevelRepository;
    private final TrainingInstanceRepository trainingInstanceRepository;
    private final TrainingRunRepository trainingRunRepository;

    /**
     * Instantiates a new Export import service.
     *
     * @param trainingDefinitionRepository the training definition repository
     * @param abstractLevelRepository      the abstract level repository
     * @param assessmentLevelRepository    the assessment level repository
     * @param infoLevelRepository          the info level repository
     * @param trainingLevelRepository      the training level repository
     * @param trainingInstanceRepository   the training instance repository
     * @param trainingRunRepository        the training run repository
     */
    @Autowired
    public ExportImportService(TrainingDefinitionRepository trainingDefinitionRepository,
                               AbstractLevelRepository abstractLevelRepository,
                               AssessmentLevelRepository assessmentLevelRepository,
                               QuestionAnswerRepository questionAnswerRepository,
                               InfoLevelRepository infoLevelRepository,
                               TrainingLevelRepository trainingLevelRepository,
                               TrainingInstanceRepository trainingInstanceRepository,
                               TrainingRunRepository trainingRunRepository)
    {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.abstractLevelRepository = abstractLevelRepository;
        this.assessmentLevelRepository = assessmentLevelRepository;
        this.questionAnswerRepository = questionAnswerRepository;
        this.trainingLevelRepository = trainingLevelRepository;
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
        return trainingDefinitionRepository.findById(trainingDefinitionId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class, "id", trainingDefinitionId.getClass(),
                        trainingDefinitionId)));
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
            assessmentLevelRepository.save((AssessmentLevel) level);
        } else if (level instanceof InfoLevel) {
            infoLevelRepository.save((InfoLevel) level);
        } else {
            trainingLevelRepository.save((TrainingLevel) level);
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
                        trainingInstanceId)));
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
     * Gets all answers given by participant to assessment questions.
     *
     * @param trainingRunId the pool id
     * @return the sandbox definition id
     */
    public Map<Long, List<QuestionAnswer>> findQuestionsAnswersOfAssessment(Long trainingRunId) {
        return questionAnswerRepository.getAllByTrainingRunId(trainingRunId).stream()
                .collect(Collectors.groupingBy(questionAnswer -> questionAnswer.getQuestion().getAssessmentLevel().getId()));
    }
}
