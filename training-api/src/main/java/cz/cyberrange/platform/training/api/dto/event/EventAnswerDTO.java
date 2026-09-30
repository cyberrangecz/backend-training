package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Base type for a single trainee answer to one assessment question exposed on the {@code
 * assessment_answered} event response. Holds the answered question reference; each concrete subtype
 * adds the answer value in the shape appropriate to its question type. The {@code type} property
 * discriminates the subtype.
 */
@Data
@Schema(
    description = "One trainee answer to a single assessment question.",
    discriminatorMapping = {
      @DiscriminatorMapping(
          value = FreeFormEventAnswerDTO.TYPE,
          schema = FreeFormEventAnswerDTO.class),
      @DiscriminatorMapping(
          value = MultipleChoiceEventAnswerDTO.TYPE,
          schema = MultipleChoiceEventAnswerDTO.class),
      @DiscriminatorMapping(
          value = ExtendedMatchingEventAnswerDTO.TYPE,
          schema = ExtendedMatchingEventAnswerDTO.class)
    })
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

  @Schema(example = "1")
  @JsonProperty("question_id")
  private Long questionId;

  @Schema(
      example = "true",
      description = "Whether the answer to the question as a whole is correct.")
  @JsonProperty("correct")
  private Boolean correct;

  @Schema(example = "5")
  @JsonProperty("points_gained")
  private Integer pointsGained;
}
