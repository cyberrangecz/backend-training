package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.MitreTechnique;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.question.QuestionAnswer;
import cz.cyberrange.platform.training.persistence.repository.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Reads the training definitions, levels, instances, runs and assessment answers that make up an
 * export or an archive, and writes the levels of an imported definition back
 */
@Service
public class ExportImportService {

  private final TrainingDefinitionRepository trainingDefinitionRepository;
  private final AbstractLevelRepository abstractLevelRepository;
  private final AssessmentLevelRepository assessmentLevelRepository;
  private final QuestionAnswerRepository questionAnswerRepository;
  private final InfoLevelRepository infoLevelRepository;
  private final TrainingLevelRepository trainingLevelRepository;
  private final MitreTechniqueRepository mitreTechniqueRepository;
  private final AccessLevelRepository accessLevelRepository;
  private final TrainingInstanceRepository trainingInstanceRepository;
  private final TrainingRunRepository trainingRunRepository;

  /**
   * Creates the service with the repositories for definitions, levels, instances, runs and
   * assessment answers it reads and writes
   */
  @Autowired
  public ExportImportService(
      TrainingDefinitionRepository trainingDefinitionRepository,
      AbstractLevelRepository abstractLevelRepository,
      AssessmentLevelRepository assessmentLevelRepository,
      QuestionAnswerRepository questionAnswerRepository,
      InfoLevelRepository infoLevelRepository,
      TrainingLevelRepository trainingLevelRepository,
      MitreTechniqueRepository mitreTechniqueRepository,
      AccessLevelRepository accessLevelRepository,
      TrainingInstanceRepository trainingInstanceRepository,
      TrainingRunRepository trainingRunRepository) {
    this.trainingDefinitionRepository = trainingDefinitionRepository;
    this.abstractLevelRepository = abstractLevelRepository;
    this.assessmentLevelRepository = assessmentLevelRepository;
    this.questionAnswerRepository = questionAnswerRepository;
    this.trainingLevelRepository = trainingLevelRepository;
    this.accessLevelRepository = accessLevelRepository;
    this.mitreTechniqueRepository = mitreTechniqueRepository;
    this.infoLevelRepository = infoLevelRepository;
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.trainingRunRepository = trainingRunRepository;
  }

  /**
   * Finds the training definition carrying the given id, its authors and its beta testing group's
   * organizers loaded along with it. Levels are not reachable from the definition and are read
   * through their own repository.
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
   * Persists the given level as the last one of the given definition, ordering it one past the
   * definition's current highest level order and saving it through the repository of its concrete
   * level type. A training level additionally has its MITRE techniques reconciled against those
   * already stored.
   *
   * @param level the {@link AbstractLevel} to persist
   * @param definition the {@link TrainingDefinition} the level becomes part of
   */
  public void createLevel(AbstractLevel level, TrainingDefinition definition) {
    level.setOrder(abstractLevelRepository.getCurrentMaxOrder(definition.getId()) + 1);
    level.setTrainingDefinition(definition);
    if (level instanceof AssessmentLevel) {
      assessmentLevelRepository.save((AssessmentLevel) level);
    } else if (level instanceof InfoLevel) {
      infoLevelRepository.save((InfoLevel) level);
    } else if (level instanceof AccessLevel) {
      accessLevelRepository.save((AccessLevel) level);
    } else {
      setMitreTechniques((TrainingLevel) level);
      trainingLevelRepository.save((TrainingLevel) level);
    }
  }

  /**
   * Replaces the level's MITRE techniques with the union of those already stored under the same
   * technique keys and the level's own instances, so that a key the database already knows is
   * reused instead of stored a second time, and registers the level on each technique it keeps.
   *
   * @param importedLevel the level whose techniques are reconciled in place
   */
  private void setMitreTechniques(TrainingLevel importedLevel) {
    Set<String> techniqueKeys =
        importedLevel.getMitreTechniques().stream()
            .map(MitreTechnique::getTechniqueKey)
            .collect(Collectors.toSet());
    Set<MitreTechnique> resultTechniques =
        mitreTechniqueRepository.findAllByTechniqueKeyIn(techniqueKeys);
    resultTechniques.addAll(importedLevel.getMitreTechniques());

    importedLevel.setMitreTechniques(new HashSet<>());
    resultTechniques.forEach(importedLevel::addMitreTechnique);
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
    return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId);
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
    return questionAnswerRepository.getAllByTrainingRunId(trainingRunId).stream()
        .collect(
            Collectors.groupingBy(
                questionAnswer -> questionAnswer.getQuestion().getAssessmentLevel().getId()));
  }
}
