package cz.cyberrange.platform.training.rest.integration;

import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.TRAcquisitionLock;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.AssessmentType;
import cz.cyberrange.platform.training.persistence.model.enums.QuestionType;
import cz.cyberrange.platform.training.persistence.model.enums.SubmissionType;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.model.question.QuestionAnswer;
import cz.cyberrange.platform.training.persistence.model.question.QuestionChoice;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Commits training definitions, levels, instances, runs and their dependants straight to the test
 * database, so that the application's own transactions (including the authorization checks that run
 * in separate transactions) see them. Every method returns the persisted entity with its generated
 * primary key; callers read relations by id, never through the returned entity.
 */
final class TrainingRunScenarioSeeder {

  static final String TRAINING_LEVEL_ANSWER = "correct-answer";
  static final String TRAINING_LEVEL_SOLUTION = "the solution text";
  static final String ACCESS_LEVEL_PASSKEY = "open-sesame";
  static final int TRAINING_LEVEL_MAX_SCORE = 10;
  static final int TRAINING_LEVEL_INCORRECT_ANSWER_LIMIT = 2;
  static final int HINT_PENALTY = 3;

  private final TransactionTemplate transactionTemplate;
  private final EntityManager entityManager;
  private final JdbcTemplate jdbcTemplate;

  TrainingRunScenarioSeeder(
      TransactionTemplate transactionTemplate,
      EntityManager entityManager,
      JdbcTemplate jdbcTemplate) {
    this.transactionTemplate = transactionTemplate;
    this.entityManager = entityManager;
    this.jdbcTemplate = jdbcTemplate;
  }

  /** Empties every application table, leaving the Flyway history untouched. */
  void deleteAll() {
    List<String> tableNames =
        jdbcTemplate.queryForList(
            "SELECT tablename FROM pg_tables WHERE schemaname = 'public'"
                + " AND tablename <> 'flyway_schema_history'",
            String.class);
    if (!tableNames.isEmpty()) {
      jdbcTemplate.execute("TRUNCATE TABLE " + String.join(", ", tableNames) + " CASCADE");
    }
  }

  UserRef userRef(long userRefId) {
    return inTransaction(
        () -> {
          UserRef userRef = new UserRef();
          userRef.setUserRefId(userRefId);
          entityManager.persist(userRef);
          return userRef;
        });
  }

  TrainingDefinition definition(String title, UserRef... authors) {
    return inTransaction(
        () -> {
          TrainingDefinition definition = new TrainingDefinition();
          definition.setTitle(title);
          definition.setDescription("description of " + title);
          definition.setState(TDState.RELEASED);
          definition.setEstimatedDuration(30);
          definition.setLastEdited(now());
          definition.setLastEditedBy("seeder");
          definition.setCreatedAt(now());
          Set<UserRef> managedAuthors = new HashSet<>();
          for (UserRef author : authors) {
            managedAuthors.add(entityManager.find(UserRef.class, author.getId()));
          }
          definition.setAuthors(managedAuthors);
          entityManager.persist(definition);
          return definition;
        });
  }

  InfoLevel infoLevel(TrainingDefinition definition, int order) {
    return inTransaction(
        () -> {
          InfoLevel level = new InfoLevel();
          prepareLevel(level, definition, order, "info level " + order, 0);
          level.setContent("info content " + order);
          entityManager.persist(level);
          return level;
        });
  }

  /** Persists a training level with a static answer, one hint and a two attempt limit. */
  TrainingLevel trainingLevel(TrainingDefinition definition, int order, boolean solutionPenalized) {
    return inTransaction(
        () -> {
          TrainingLevel level = new TrainingLevel();
          prepareLevel(
              level, definition, order, "training level " + order, TRAINING_LEVEL_MAX_SCORE);
          level.setAnswer(TRAINING_LEVEL_ANSWER);
          level.setContent("training content " + order);
          level.setSolution(TRAINING_LEVEL_SOLUTION);
          level.setSolutionPenalized(solutionPenalized);
          level.setIncorrectAnswerLimit(TRAINING_LEVEL_INCORRECT_ANSWER_LIMIT);
          level.setVariantAnswers(false);
          level.setCommandsRequired(false);
          level.setExpectedCommands(new HashSet<>());
          Hint hint = new Hint();
          hint.setTitle("hint title");
          hint.setContent("hint content");
          hint.setHintPenalty(HINT_PENALTY);
          hint.setOrder(0);
          level.addHint(hint);
          entityManager.persist(level);
          return level;
        });
  }

  /** Persists a training level whose correct answer is read from answers-storage by variable. */
  TrainingLevel variantTrainingLevel(
      TrainingDefinition definition, int order, String answerVariableName, String solution) {
    return inTransaction(
        () -> {
          TrainingLevel level = new TrainingLevel();
          prepareLevel(
              level, definition, order, "variant level " + order, TRAINING_LEVEL_MAX_SCORE);
          level.setAnswer(null);
          level.setAnswerVariableName(answerVariableName);
          level.setContent("variant content");
          level.setSolution(solution);
          level.setSolutionPenalized(false);
          level.setIncorrectAnswerLimit(TRAINING_LEVEL_INCORRECT_ANSWER_LIMIT);
          level.setVariantAnswers(true);
          level.setCommandsRequired(false);
          level.setExpectedCommands(new HashSet<>());
          entityManager.persist(level);
          return level;
        });
  }

  AccessLevel accessLevel(TrainingDefinition definition, int order) {
    return inTransaction(
        () -> {
          AccessLevel level = new AccessLevel();
          prepareLevel(level, definition, order, "access level " + order, 0);
          level.setPasskey(ACCESS_LEVEL_PASSKEY);
          level.setCloudContent("cloud content");
          level.setLocalContent(
              "token=${ACCESS_TOKEN} user=${USER_ID} definition=${SANDBOX_DEFINITION_ID}");
          entityManager.persist(level);
          return level;
        });
  }

  /**
   * Persists an assessment level of the given type holding a free-form question (accepted answer
   * "alpha", 5 points, 1 penalty), a multiple-choice question (correct choice "red", wrong choice
   * "blue", 5 points, 1 penalty) and an extended matching question (statement 0 matches option 1, 5
   * points, 1 penalty). The free-form question is the only one that may be left unanswered in a
   * questionnaire, when {@code questionnaireRequiresAnswers} is false.
   */
  AssessmentLevel assessmentLevel(
      TrainingDefinition definition,
      int order,
      AssessmentType assessmentType,
      boolean questionnaireRequiresAnswers) {
    return inTransaction(
        () -> {
          AssessmentLevel level = new AssessmentLevel();
          prepareLevel(level, definition, order, "assessment level " + order, 15);
          level.setInstructions("instructions");
          level.setAssessmentType(assessmentType);
          Question freeForm = question(QuestionType.FFQ, "free-form question", 0);
          QuestionChoice accepted = choice("alpha", true, 0);
          freeForm.setChoices(new ArrayList<>(List.of(accepted)));
          freeForm.setAnswerRequired(
              assessmentType == AssessmentType.TEST || questionnaireRequiresAnswers);
          Question multipleChoice = question(QuestionType.MCQ, "multiple-choice question", 1);
          multipleChoice.setChoices(
              new ArrayList<>(List.of(choice("red", true, 0), choice("blue", false, 1))));
          multipleChoice.setAnswerRequired(true);
          Question matching = question(QuestionType.EMI, "matching question", 2);
          ExtendedMatchingOption firstOption = option("first option", 0);
          ExtendedMatchingOption secondOption = option("second option", 1);
          matching.setExtendedMatchingOptions(new ArrayList<>(List.of(firstOption, secondOption)));
          ExtendedMatchingStatement statement = new ExtendedMatchingStatement();
          statement.setText("statement");
          statement.setOrder(0);
          statement.setExtendedMatchingOption(secondOption);
          matching.setExtendedMatchingStatements(new ArrayList<>(List.of(statement)));
          matching.setAnswerRequired(true);
          level.setQuestions(new ArrayList<>(List.of(freeForm, multipleChoice, matching)));
          entityManager.persist(level);
          return level;
        });
  }

  TrainingInstance instance(
      TrainingDefinition definition,
      String accessToken,
      Long poolId,
      boolean localEnvironment,
      LocalDateTime startTime,
      LocalDateTime endTime,
      UserRef... organizers) {
    return inTransaction(
        () -> {
          TrainingInstance instance = new TrainingInstance();
          instance.setTitle("instance " + accessToken);
          instance.setAccessToken(accessToken);
          instance.setPoolId(poolId);
          instance.setLocalEnvironment(localEnvironment);
          instance.setSandboxDefinitionId(77L);
          instance.setStartTime(startTime);
          instance.setEndTime(endTime);
          instance.setLastEdited(now());
          instance.setLastEditedBy("seeder");
          instance.setShowStepperBar(true);
          instance.setBackwardMode(false);
          instance.setTrainingDefinition(
              entityManager.getReference(TrainingDefinition.class, definition.getId()));
          Set<UserRef> managedOrganizers = new HashSet<>();
          for (UserRef organizer : organizers) {
            managedOrganizers.add(entityManager.find(UserRef.class, organizer.getId()));
          }
          instance.setOrganizers(managedOrganizers);
          entityManager.persist(instance);
          return instance;
        });
  }

  /** Persists a run of the participant standing on the given level, started an hour ago. */
  TrainingRun run(
      TrainingInstance instance,
      UserRef participant,
      AbstractLevel currentLevel,
      TRState state,
      String sandboxInstanceRefId,
      boolean levelAnswered) {
    return inTransaction(
        () -> {
          TrainingRun run = new TrainingRun();
          run.setTrainingInstance(
              entityManager.getReference(TrainingInstance.class, instance.getId()));
          run.setParticipantRef(entityManager.getReference(UserRef.class, participant.getId()));
          run.setCurrentLevel(
              entityManager.getReference(AbstractLevel.class, currentLevel.getId()));
          run.setState(state);
          run.setSandboxInstanceRefId(sandboxInstanceRefId);
          run.setLevelAnswered(levelAnswered);
          run.setStartTime(now().minusHours(1));
          run.setEndTime(instance.getEndTime());
          run.setAssessmentResponses("[]");
          run.setMaxLevelScore(currentLevel.getMaxScore());
          entityManager.persist(run);
          return run;
        });
  }

  void lock(long participantUserRefId, long trainingInstanceId) {
    inTransaction(
        () -> {
          entityManager.persist(
              new TRAcquisitionLock(participantUserRefId, trainingInstanceId, now()));
          return null;
        });
  }

  void submission(TrainingRun run, AbstractLevel level, SubmissionType type, String provided) {
    inTransaction(
        () -> {
          Submission submission = new Submission();
          submission.setTrainingRun(entityManager.getReference(TrainingRun.class, run.getId()));
          submission.setLevel(entityManager.getReference(AbstractLevel.class, level.getId()));
          submission.setType(type);
          submission.setProvided(provided);
          submission.setDate(now());
          submission.setIpAddress("");
          entityManager.persist(submission);
          return null;
        });
  }

  void questionAnswer(TrainingRun run, long questionId, String answer) {
    inTransaction(
        () -> {
          QuestionAnswer questionAnswer =
              new QuestionAnswer(
                  entityManager.getReference(Question.class, questionId),
                  entityManager.getReference(TrainingRun.class, run.getId()));
          questionAnswer.setAnswers(new HashSet<>(Set.of(answer)));
          entityManager.persist(questionAnswer);
          return null;
        });
  }

  /** Overwrites run columns the application owns, such as scores, penalties and flags. */
  void updateRun(long runId, String assignments) {
    jdbcTemplate.update("UPDATE training_run SET " + assignments + " WHERE id = ?", runId);
  }

  static LocalDateTime now() {
    return LocalDateTime.now(Clock.systemUTC());
  }

  private static void prepareLevel(
      AbstractLevel level, TrainingDefinition definition, int order, String title, int maxScore) {
    level.setTitle(title);
    level.setOrder(order);
    level.setMaxScore(maxScore);
    level.setEstimatedDuration(5);
    level.setTrainingDefinition(definition);
  }

  private static Question question(QuestionType type, String text, int order) {
    Question question = new Question();
    question.setQuestionType(type);
    question.setText(text);
    question.setOrder(order);
    question.setPoints(5);
    question.setPenalty(1);
    return question;
  }

  private static QuestionChoice choice(String text, boolean correct, int order) {
    QuestionChoice choice = new QuestionChoice();
    choice.setText(text);
    choice.setCorrect(correct);
    choice.setOrder(order);
    return choice;
  }

  private static ExtendedMatchingOption option(String text, int order) {
    ExtendedMatchingOption option = new ExtendedMatchingOption();
    option.setText(text);
    option.setOrder(order);
    return option;
  }

  private <T> T inTransaction(java.util.function.Supplier<T> action) {
    return transactionTemplate.execute(status -> action.get());
  }
}
