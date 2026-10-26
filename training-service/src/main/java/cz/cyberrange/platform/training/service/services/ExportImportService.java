package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.question.QuestionAnswer;
import cz.cyberrange.platform.training.persistence.repository.QuestionAnswerRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingDefinitionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Reads the training definitions, instances, runs and assessment answers that make up an export or
 * an archive.
 */
@Service
public class ExportImportService {

  private final TrainingDefinitionRepository trainingDefinitionRepository;
  private final QuestionAnswerRepository questionAnswerRepository;
  private final TrainingInstanceRepository trainingInstanceRepository;
  private final TrainingRunRepository trainingRunRepository;

  /**
   * Creates the service with the repositories for definitions, instances, runs and assessment
   * answers it reads.
   *
   * @param trainingDefinitionRepository repository of training definitions
   * @param questionAnswerRepository repository of assessment answers
   * @param trainingInstanceRepository repository of training instances
   * @param trainingRunRepository repository of training runs
   */
  @Autowired
  public ExportImportService(
      TrainingDefinitionRepository trainingDefinitionRepository,
      QuestionAnswerRepository questionAnswerRepository,
      TrainingInstanceRepository trainingInstanceRepository,
      TrainingRunRepository trainingRunRepository) {
    this.trainingDefinitionRepository = trainingDefinitionRepository;
    this.questionAnswerRepository = questionAnswerRepository;
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.trainingRunRepository = trainingRunRepository;
  }

  /**
   * Finds the training definition carrying the given id, its authors loaded along with it. Levels
   * are not reachable from the definition and are read through their own repository.
   *
   * @param trainingDefinitionId id of the definition to look up
   * @return the {@link TrainingDefinition} carrying that id
   * @throws EntityNotFoundException when no definition carries that id
   */
  public TrainingDefinition findById(Long trainingDefinitionId) {
    return trainingDefinitionRepository
        .findById(trainingDefinitionId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        TrainingDefinition.class,
                        "id",
                        trainingDefinitionId.getClass(),
                        trainingDefinitionId)));
  }

  /**
   * Finds the training instance carrying the given id, its organizers and its training definition
   * together with that definition's authors loaded along with it.
   *
   * @param trainingInstanceId id of the instance to look up
   * @return the {@link TrainingInstance} carrying that id
   * @throws EntityNotFoundException when no instance carries that id
   */
  public TrainingInstance findInstanceById(Long trainingInstanceId) {
    return trainingInstanceRepository
        .findById(trainingInstanceId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        TrainingInstance.class,
                        "id",
                        trainingInstanceId.getClass(),
                        trainingInstanceId)));
  }

  /**
   * Finds every training run of the given training instance, each one's participant reference
   * loaded along with it. The runs come back in no guaranteed order.
   *
   * @param trainingInstanceId id of the instance whose runs are wanted
   * @return that instance's {@link TrainingRun}s, empty when it has none
   */
  public Set<TrainingRun> findRunsByInstanceId(Long trainingInstanceId) {
    return trainingRunRepository.findAllByTrainingInstanceIdOrderById(trainingInstanceId);
  }

  /**
   * Collects every answer recorded in the given training run, grouped by the assessment level the
   * answered question belongs to.
   *
   * @param trainingRunId id of the training run whose answers are wanted
   * @return the run's {@link QuestionAnswer}s keyed by assessment level id, empty when the run
   *     recorded none
   */
  public Map<Long, List<QuestionAnswer>> findQuestionsAnswersOfAssessment(Long trainingRunId) {
    return questionAnswerRepository.getAllByTrainingRunIdOrderByQuestionId(trainingRunId).stream()
        .collect(
            Collectors.groupingBy(
                questionAnswer -> questionAnswer.getQuestion().getAssessmentLevel().getId()));
  }
}
