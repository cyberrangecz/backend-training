package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Base type for a single trainee answer to one assessment question exposed on the {@code
 * assessment_answered} event response. Holds the answered question reference; each concrete subtype
 * adds the answer value in the shape appropriate to its question type. The {@code type} property
 * discriminates the subtype.
 */
@Getter
@Setter
@ToString
@ApiModel(
    value = "EventAnswerDTO",
    description = "A single trainee answer discriminated by question type")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = FreeFormEventAnswerDTO.class, name = FreeFormEventAnswerDTO.TYPE),
  @JsonSubTypes.Type(
      value = MultipleChoiceEventAnswerDTO.class,
      name = MultipleChoiceEventAnswerDTO.TYPE),
  @JsonSubTypes.Type(
      value = ExtendedMatchingEventAnswerDTO.class,
      name = ExtendedMatchingEventAnswerDTO.TYPE),
})
public abstract class EventAnswerDTO {

  @ApiModelProperty(value = "ID of the answered question", example = "1")
  @JsonProperty("question_id")
  private Long questionId;

  @ApiModelProperty(
      value = "Whether the answer was correct; null when the assessment is not scored",
      example = "true")
  @JsonProperty("correct")
  private Boolean correct;

  @ApiModelProperty(
      value = "Net points gained for the answer; null when the assessment is not scored",
      example = "5")
  @JsonProperty("points_gained")
  private Integer pointsGained;
}
