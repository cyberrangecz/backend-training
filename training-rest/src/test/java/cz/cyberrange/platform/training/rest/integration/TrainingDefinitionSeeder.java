package cz.cyberrange.platform.training.rest.integration;

import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.BetaTestingGroup;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.MitreTechnique;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.AssessmentType;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.MitreTechniqueRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingDefinitionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Persists training definitions and everything hanging off them, each call in its own committed
 * transaction so that the application's independent security transactions see the rows. Users are
 * addressed by their cross-service id and levels, instances and definitions by primary key.
 */
final class TrainingDefinitionSeeder {

  static final LocalDateTime SEEDED_TIME = LocalDateTime.of(2026, 1, 15, 10, 30, 45);
  static final String SEEDED_EDITOR = "Seeded Editor";

  private final TransactionTemplate transaction;
  private final JdbcTemplate jdbcTemplate;
  private final TrainingDefinitionRepository trainingDefinitionRepository;
  private final AbstractLevelRepository abstractLevelRepository;
  private final UserRefRepository userRefRepository;
  private final MitreTechniqueRepository mitreTechniqueRepository;
  private final TrainingInstanceRepository trainingInstanceRepository;
  private final TrainingRunRepository trainingRunRepository;

  TrainingDefinitionSeeder(ApplicationContext context) {
    this.transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
    this.jdbcTemplate = context.getBean(JdbcTemplate.class);
    this.trainingDefinitionRepository = context.getBean(TrainingDefinitionRepository.class);
    this.abstractLevelRepository = context.getBean(AbstractLevelRepository.class);
    this.userRefRepository = context.getBean(UserRefRepository.class);
    this.mitreTechniqueRepository = context.getBean(MitreTechniqueRepository.class);
    this.trainingInstanceRepository = context.getBean(TrainingInstanceRepository.class);
    this.trainingRunRepository = context.getBean(TrainingRunRepository.class);
  }

  /** Removes every row the seeder and the application created. */
  void clear() {
    jdbcTemplate.execute(
        "TRUNCATE TABLE training_definition, user_ref, beta_testing_group, mitre_technique CASCADE");
  }

  /**
   * Stores a definition authored by the given users.
   *
   * @param title title of the definition
   * @param state lifecycle state of the definition
   * @param authorUserRefIds cross-service ids of its authors
   * @return primary key of the definition
   */
  Long definition(String title, TDState state, long... authorUserRefIds) {
    return transaction.execute(
        status -> {
          TrainingDefinition definition = new TrainingDefinition();
          definition.setTitle(title);
          definition.setDescription("Description of " + title);
          definition.setState(state);
          definition.setLastEdited(SEEDED_TIME);
          definition.setLastEditedBy(SEEDED_EDITOR);
          definition.setCreatedAt(SEEDED_TIME);
          definition.setEstimatedDuration(0);
          for (long authorUserRefId : authorUserRefIds) {
            definition.addAuthor(userRefRepository.createOrGet(authorUserRefId));
          }
          return trainingDefinitionRepository.save(definition).getId();
        });
  }

  /**
   * Gives a definition a beta testing group made of the given organizers.
   *
   * @param definitionId primary key of the definition
   * @param organizerUserRefIds cross-service ids of the group's organizers
   */
  void betaTestingGroup(Long definitionId, long... organizerUserRefIds) {
    transaction.executeWithoutResult(
        status -> {
          BetaTestingGroup group = new BetaTestingGroup();
          for (long organizerUserRefId : organizerUserRefIds) {
            group.addOrganizer(userRefRepository.createOrGet(organizerUserRefId));
          }
          TrainingDefinition definition = trainingDefinitionRepository.findById(definitionId).get();
          definition.setBetaTestingGroup(group);
          trainingDefinitionRepository.save(definition);
        });
  }

  /** Stores an info level at the given position and adds its duration to the definition. */
  Long infoLevel(Long definitionId, int order, long estimatedDuration) {
    return transaction.execute(
        status -> {
          InfoLevel level = new InfoLevel();
          level.setTitle("Info level " + order);
          level.setContent("Content of info level " + order);
          return saveLevel(level, definitionId, order, estimatedDuration).getId();
        });
  }

  /** Stores an access level at the given position and adds its duration to the definition. */
  Long accessLevel(Long definitionId, int order, long estimatedDuration) {
    return transaction.execute(
        status -> {
          AccessLevel level = new AccessLevel();
          level.setTitle("Access level " + order);
          level.setPasskey("passkey-" + order);
          level.setCloudContent("Cloud content " + order);
          level.setLocalContent("Local content " + order);
          return saveLevel(level, definitionId, order, estimatedDuration).getId();
        });
  }

  /** Stores a questionnaire assessment level at the given position. */
  Long assessmentLevel(Long definitionId, int order, long estimatedDuration) {
    return transaction.execute(
        status -> {
          AssessmentLevel level = new AssessmentLevel();
          level.setTitle("Assessment level " + order);
          level.setInstructions("Instructions " + order);
          level.setAssessmentType(AssessmentType.QUESTIONNAIRE);
          return saveLevel(level, definitionId, order, estimatedDuration).getId();
        });
  }

  /**
   * Stores a training level at the given position, attaching the MITRE techniques with the given
   * keys.
   */
  Long trainingLevel(
      Long definitionId, int order, long estimatedDuration, String... mitreTechniqueKeys) {
    return transaction.execute(
        status -> {
          TrainingLevel level = newTrainingLevel(order);
          attachMitreTechniques(level, mitreTechniqueKeys);
          return saveLevel(level, definitionId, order, estimatedDuration).getId();
        });
  }

  /**
   * Stores a training level at the given position carrying one hint per given title.
   *
   * @return primary key of the level
   */
  Long trainingLevelWithHints(Long definitionId, int order, String... hintTitles) {
    return transaction.execute(
        status -> {
          TrainingLevel level = newTrainingLevel(order);
          int hintOrder = 0;
          for (String hintTitle : hintTitles) {
            Hint hint = new Hint();
            hint.setTitle(hintTitle);
            hint.setContent("Content of " + hintTitle);
            hint.setHintPenalty(5);
            hint.setOrder(hintOrder++);
            hint.setTrainingLevel(level);
            level.addHint(hint);
          }
          return saveLevel(level, definitionId, order, 1).getId();
        });
  }

  /**
   * Returns the primary keys of the hints of a training level, ordered by their position.
   *
   * @param trainingLevelId primary key of the level
   */
  List<Long> hintIds(Long trainingLevelId) {
    return jdbcTemplate.queryForList(
        "SELECT id FROM hint WHERE training_level_id = ? ORDER BY order_in_level",
        Long.class,
        trainingLevelId);
  }

  /**
   * Stores a training instance of a definition.
   *
   * @param definitionId primary key of the definition
   * @param endTime when the instance ends
   * @param organizerUserRefIds cross-service ids of the instance's organizers
   * @return primary key of the instance
   */
  Long instance(Long definitionId, LocalDateTime endTime, long... organizerUserRefIds) {
    return transaction.execute(
        status -> {
          TrainingInstance instance = new TrainingInstance();
          instance.setTitle("Instance of " + definitionId);
          instance.setStartTime(endTime.minusDays(2));
          instance.setEndTime(endTime);
          instance.setAccessToken("token-" + UUID.randomUUID());
          instance.setLastEdited(SEEDED_TIME);
          instance.setLastEditedBy(SEEDED_EDITOR);
          instance.setTrainingDefinition(trainingDefinitionRepository.findById(definitionId).get());
          Set<UserRef> organizers = new HashSet<>();
          Arrays.stream(organizerUserRefIds)
              .forEach(userRefId -> organizers.add(userRefRepository.createOrGet(userRefId)));
          instance.setOrganizers(organizers);
          return trainingInstanceRepository.save(instance).getId();
        });
  }

  /**
   * Stores a finished training run of an instance for a participant.
   *
   * @param instanceId primary key of the instance
   * @param currentLevelId primary key of the level the run stands at
   * @param participantUserRefId cross-service id of the participant
   */
  void run(Long instanceId, Long currentLevelId, long participantUserRefId) {
    transaction.executeWithoutResult(
        status -> {
          TrainingRun run = new TrainingRun();
          run.setStartTime(SEEDED_TIME);
          run.setEndTime(SEEDED_TIME.plusHours(1));
          run.setState(TRState.FINISHED);
          run.setCurrentLevel(abstractLevelRepository.findById(currentLevelId).get());
          run.setTrainingInstance(trainingInstanceRepository.findById(instanceId).get());
          run.setParticipantRef(userRefRepository.createOrGet(participantUserRefId));
          trainingRunRepository.save(run);
        });
  }

  /**
   * Returns the cross-service ids of the authors of a definition.
   *
   * @param definitionId primary key of the definition
   */
  Set<Long> authorUserRefIds(Long definitionId) {
    return new HashSet<>(
        jdbcTemplate.queryForList(
            "SELECT u.user_ref_id FROM user_ref u JOIN training_definition_user_ref a"
                + " ON a.user_ref_id = u.id WHERE a.training_definition_id = ?",
            Long.class,
            definitionId));
  }

  /**
   * Returns the cross-service ids of the organizers in the beta testing group of a definition.
   *
   * @param definitionId primary key of the definition
   */
  Set<Long> betaTesterUserRefIds(Long definitionId) {
    return new HashSet<>(
        jdbcTemplate.queryForList(
            "SELECT u.user_ref_id FROM user_ref u JOIN beta_testing_group_user_ref b"
                + " ON b.user_ref_id = u.id JOIN training_definition d"
                + " ON d.beta_testing_group_id = b.beta_testing_group_id WHERE d.id = ?",
            Long.class,
            definitionId));
  }

  /** Returns how many definitions are stored. */
  long definitionCount() {
    return jdbcTemplate.queryForObject("SELECT count(*) FROM training_definition", Long.class);
  }

  private TrainingLevel newTrainingLevel(int order) {
    TrainingLevel level = new TrainingLevel();
    level.setTitle("Training level " + order);
    level.setMaxScore(100);
    level.setAnswer("answer-" + order);
    level.setContent("Content of training level " + order);
    level.setSolution("Solution of training level " + order);
    level.setIncorrectAnswerLimit(10);
    return level;
  }

  private void attachMitreTechniques(TrainingLevel level, String... mitreTechniqueKeys) {
    for (String techniqueKey : mitreTechniqueKeys) {
      MitreTechnique technique =
          mitreTechniqueRepository
              .findByTechniqueKey(techniqueKey)
              .orElseGet(
                  () -> {
                    MitreTechnique created = new MitreTechnique();
                    created.setTechniqueKey(techniqueKey);
                    return mitreTechniqueRepository.save(created);
                  });
      level.addMitreTechnique(technique);
    }
  }

  private <L extends AbstractLevel> L saveLevel(
      L level, Long definitionId, int order, long estimatedDuration) {
    TrainingDefinition definition = trainingDefinitionRepository.findById(definitionId).get();
    level.setTrainingDefinition(definition);
    level.setOrder(order);
    level.setEstimatedDuration(estimatedDuration);
    definition.setEstimatedDuration(definition.getEstimatedDuration() + estimatedDuration);
    return abstractLevelRepository.save(level);
  }
}
