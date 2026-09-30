package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Parent class for every training audit event. It registers one subtype per concrete event kind
 * under that kind's own {@code type} discriminator value, the same short constant the corresponding
 * audit model class declares.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(
    description = "Common shape of every audit event recorded for a training run.",
    subTypes = {
      AssessmentAnsweredDTO.class,
      CorrectAnswerSubmittedDTO.class,
      WrongAnswerSubmittedDTO.class,
      HintTakenDTO.class,
      LevelCompletedDTO.class,
      LevelStartedDTO.class,
      SolutionDisplayedDTO.class,
      TrainingRunFinishedDTO.class,
      TrainingRunResumedDTO.class,
      TrainingRunStartedDTO.class
    },
    discriminatorMapping = {
      @DiscriminatorMapping(value = "assessment_answered", schema = AssessmentAnsweredDTO.class),
      @DiscriminatorMapping(
          value = "correct_answer_submitted",
          schema = CorrectAnswerSubmittedDTO.class),
      @DiscriminatorMapping(
          value = "wrong_answer_submitted",
          schema = WrongAnswerSubmittedDTO.class),
      @DiscriminatorMapping(value = "hint_taken", schema = HintTakenDTO.class),
      @DiscriminatorMapping(value = "level_completed", schema = LevelCompletedDTO.class),
      @DiscriminatorMapping(value = "level_started", schema = LevelStartedDTO.class),
      @DiscriminatorMapping(value = "solution_displayed", schema = SolutionDisplayedDTO.class),
      @DiscriminatorMapping(value = "training_run_finished", schema = TrainingRunFinishedDTO.class),
      @DiscriminatorMapping(value = "training_run_resumed", schema = TrainingRunResumedDTO.class),
      @DiscriminatorMapping(value = "training_run_started", schema = TrainingRunStartedDTO.class)
    })
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = AssessmentAnsweredDTO.class, name = "assessment_answered"),
  @JsonSubTypes.Type(value = CorrectAnswerSubmittedDTO.class, name = "correct_answer_submitted"),
  @JsonSubTypes.Type(value = WrongAnswerSubmittedDTO.class, name = "wrong_answer_submitted"),
  @JsonSubTypes.Type(value = HintTakenDTO.class, name = "hint_taken"),
  @JsonSubTypes.Type(value = LevelCompletedDTO.class, name = "level_completed"),
  @JsonSubTypes.Type(value = LevelStartedDTO.class, name = "level_started"),
  @JsonSubTypes.Type(value = SolutionDisplayedDTO.class, name = "solution_displayed"),
  @JsonSubTypes.Type(value = TrainingRunFinishedDTO.class, name = "training_run_finished"),
  @JsonSubTypes.Type(value = TrainingRunResumedDTO.class, name = "training_run_resumed"),
  @JsonSubTypes.Type(value = TrainingRunStartedDTO.class, name = "training_run_started")
})
public abstract class TrainingEventDTO extends AbstractEventDTO {

  /** Sandbox pool of the training instance, copied from {@code TrainingInstance.poolId} */
  @JsonProperty("pool_id")
  private Long poolId;

  @JsonProperty("training_definition_id")
  private Long trainingDefinitionId;

  @JsonProperty("training_instance_id")
  private Long trainingInstanceId;

  @JsonProperty("training_run_id")
  private Long trainingRunId;

  @JsonProperty("actual_score_in_level")
  private Integer actualScoreInLevel;

  private Long level;

  @JsonProperty("level_order")
  private Long levelOrder;

  /**
   * Cross-service user reference id of the training run's participant, copied from {@code
   * UserRef.userRefId}; never the participant's local primary key
   */
  @JsonProperty("user_ref_id")
  private Long userRefId;

  @JsonProperty("total_training_level_score")
  private Integer totalTrainingScore;

  @JsonProperty("total_assessment_level_score")
  private Integer totalAssessmentScore;
}
