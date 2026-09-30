package cz.cyberrange.platform.training.opensearch.events.training.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Fields shared by every training-run audit event document. The {@code type} property is the
 * event's discriminator: {@link #resolveEventType} looks up the JSON name registered for the
 * concrete class among the {@link JsonSubTypes} declared here, and Jackson uses that same
 * registration in reverse to pick a subclass when reading a document back.
 */
@ApiModel(
    value = "Parent class for all audit POJO classes",
    description =
        "This class have to be extended when some event should be saved to OpenSearch."
            + " It provides 2 member variables 'timestamp' and 'type': 'timestamp' is generated based on current time; 'type'"
            + " is defined by the `TYPE` constant in each subclass")
@JsonPropertyOrder({"type", "timestamp"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = AssessmentAnswered.class, name = AssessmentAnswered.TYPE),
  @JsonSubTypes.Type(value = CorrectAnswerSubmitted.class, name = CorrectAnswerSubmitted.TYPE),
  @JsonSubTypes.Type(value = WrongAnswerSubmitted.class, name = WrongAnswerSubmitted.TYPE),
  @JsonSubTypes.Type(value = HintTaken.class, name = HintTaken.TYPE),
  @JsonSubTypes.Type(value = LevelCompleted.class, name = LevelCompleted.TYPE),
  @JsonSubTypes.Type(value = LevelStarted.class, name = LevelStarted.TYPE),
  @JsonSubTypes.Type(value = SolutionDisplayed.class, name = SolutionDisplayed.TYPE),
  @JsonSubTypes.Type(value = TrainingRunFinished.class, name = TrainingRunFinished.TYPE),
  @JsonSubTypes.Type(value = TrainingRunResumed.class, name = TrainingRunResumed.TYPE),
  @JsonSubTypes.Type(value = TrainingRunStarted.class, name = TrainingRunStarted.TYPE),
  @JsonSubTypes.Type(
      value = WrongAnswerSubmitted.class,
      name = "cz.cyberrange.platform.events.trainings.WrongFlagSubmitted"),
})
@SuperBuilder
@Getter
public abstract class AbstractAuditPOJO {

  /** OpenSearch document identifier, assigned when an event is read back from the index */
  @JsonIgnore @Setter protected String eventId;

  /** Identifier of the sandbox instance the training run executes in */
  @JsonProperty(value = "sandbox_id", required = true)
  protected String sandboxId;

  /** Identifier of the sandbox pool the training instance draws sandboxes from */
  @JsonProperty(value = "pool_id", required = true)
  protected Long poolId;

  /** Primary key of the training definition the run's instance was created from */
  @JsonProperty(value = "training_definition_id", required = true)
  protected long trainingDefinitionId;

  /** Primary key of the training instance the run belongs to */
  @JsonProperty(value = "training_instance_id", required = true)
  protected long trainingInstanceId;

  /** Primary key of the training run the event was recorded for */
  @JsonProperty(value = "training_run_id", required = true)
  protected long trainingRunId;

  /** Milliseconds elapsed since the training run started, as of when the event was recorded */
  @JsonProperty(value = "training_time", required = true)
  @JsonAlias("game_time")
  protected long trainingTime;

  /** The player's score in the current level as of this event, after any penalties */
  @JsonProperty(value = "actual_score_in_level", required = true)
  protected int actualScoreInLevel;

  /** Primary key of the level the event occurred in */
  @JsonProperty(value = "level", required = true)
  protected long level;

  /** The level's position within the training definition it belongs to */
  @JsonProperty(value = "level_order", required = true)
  protected long levelOrder;

  /** Cross-service reference id of the training run's participant; never the local primary key */
  @JsonProperty("user_ref_id")
  protected long userRefId;

  /** Epoch-millisecond instant at which the event was written */
  @JsonProperty(value = "timestamp", required = true)
  @Setter
  protected long timestamp;

  /** Discriminator naming the concrete event type, resolved via {@link #resolveEventType} */
  @JsonProperty(value = "type", required = true)
  @Setter
  protected String type;

  /** Cumulative score across the run's training levels, as of this event */
  @JsonProperty(value = "total_training_level_score", required = true)
  @JsonAlias("total_game_level_score")
  private int totalTrainingScore;

  /** Cumulative score across the run's assessment levels, as of this event */
  @JsonProperty(value = "total_assessment_level_score", required = true)
  private int totalAssessmentScore;

  private static final Map<Class<?>, String> REGISTERED_TYPES =
      Arrays.stream(AbstractAuditPOJO.class.getAnnotation(JsonSubTypes.class).value())
          .collect(
              Collectors.toMap(
                  JsonSubTypes.Type::value,
                  JsonSubTypes.Type::name,
                  (registeredName, alias) -> registeredName));

  /**
   * Resolves the polymorphic type name under which the given event class is registered.
   *
   * @param eventClass audit event class to resolve the type name for
   * @return the registered type name of the class
   * @throws IllegalArgumentException when the class is not registered as a subtype
   */
  public static String resolveEventType(Class<? extends AbstractAuditPOJO> eventClass) {
    String registeredName = REGISTERED_TYPES.get(eventClass);
    if (registeredName == null) {
      throw new IllegalArgumentException(
          eventClass.getName() + " is not registered as an audit event subtype");
    }
    return registeredName;
  }

  protected AbstractAuditPOJO() {}

  protected AbstractAuditPOJO(long timestamp, String type) {
    this.timestamp = timestamp;
    this.type = type;
  }

  @Override
  public String toString() {
    return "AbstractAuditPOJO [timestamp=" + timestamp + ", type=" + type + "]";
  }
}
