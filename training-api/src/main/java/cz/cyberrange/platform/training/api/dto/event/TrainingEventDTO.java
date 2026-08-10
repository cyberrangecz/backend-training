package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "TrainingEventDTO", description = "Parent class for all training audit events")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(
      value = AssessmentAnsweredDTO.class,
      name = "cz.cyberrange.platform.training.opensearch.events.training.model.AssessmentAnswered"),
  @JsonSubTypes.Type(
      value = CorrectAnswerSubmittedDTO.class,
      name =
          "cz.cyberrange.platform.training.opensearch.events.training.model.CorrectAnswerSubmitted"),
  @JsonSubTypes.Type(
      value = WrongAnswerSubmittedDTO.class,
      name =
          "cz.cyberrange.platform.training.opensearch.events.training.model.WrongAnswerSubmitted"),
  @JsonSubTypes.Type(
      value = HintTakenDTO.class,
      name = "cz.cyberrange.platform.training.opensearch.events.training.model.HintTaken"),
  @JsonSubTypes.Type(
      value = LevelCompletedDTO.class,
      name = "cz.cyberrange.platform.training.opensearch.events.training.model.LevelCompleted"),
  @JsonSubTypes.Type(
      value = LevelStartedDTO.class,
      name = "cz.cyberrange.platform.training.opensearch.events.training.model.LevelStarted"),
  @JsonSubTypes.Type(
      value = SolutionDisplayedDTO.class,
      name = "cz.cyberrange.platform.training.opensearch.events.training.model.SolutionDisplayed"),
  @JsonSubTypes.Type(
      value = TrainingRunFinishedDTO.class,
      name =
          "cz.cyberrange.platform.training.opensearch.events.training.model.TrainingRunFinished"),
  @JsonSubTypes.Type(
      value = TrainingRunResumedDTO.class,
      name = "cz.cyberrange.platform.training.opensearch.events.training.model.TrainingRunResumed"),
  @JsonSubTypes.Type(
      value = TrainingRunStartedDTO.class,
      name = "cz.cyberrange.platform.training.opensearch.events.training.model.TrainingRunStarted")
})
public abstract class TrainingEventDTO extends AbstractEventDTO {

  @ApiModelProperty(value = "Pool ID")
  @JsonProperty("pool_id")
  private Long poolId;

  @ApiModelProperty(value = "Training Definition ID")
  @JsonProperty("training_definition_id")
  private Long trainingDefinitionId;

  @ApiModelProperty(value = "Training Instance ID")
  @JsonProperty("training_instance_id")
  private Long trainingInstanceId;

  @ApiModelProperty(value = "Training Run ID")
  @JsonProperty("training_run_id")
  private Long trainingRunId;

  @ApiModelProperty(value = "Actual score in level")
  @JsonProperty("actual_score_in_level")
  private Integer actualScoreInLevel;

  @ApiModelProperty(value = "Level ID")
  private Long level;

  @ApiModelProperty(value = "Level order")
  @JsonProperty("level_order")
  private Long levelOrder;

  @ApiModelProperty(value = "User reference ID")
  @JsonProperty("user_ref_id")
  private Long userRefId;

  @ApiModelProperty(value = "Total training level score")
  @JsonProperty("total_training_level_score")
  private Integer totalTrainingScore;

  @ApiModelProperty(value = "Total assessment level score")
  @JsonProperty("total_assessment_level_score")
  private Integer totalAssessmentScore;
}
