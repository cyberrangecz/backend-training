package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelDTO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Encapsulates information about abstract level. Extended by {@link AssessmentLevelDTO}, {@link
 * TrainingLevelDTO}, {@link AccessLevelDTO} and {@link InfoLevelDTO}
 *
 * <p>Lists those four subtypes through {@code @JsonSubTypes} but declares no {@code @JsonTypeInfo}
 * of its own, so that listing carries no wire discriminator; the concrete subtype is whichever one
 * the caller already holds, not one Jackson resolves from a type property.
 */
@Schema(
    description = "One level of a training definition, in full detail.",
    subTypes = {
      TrainingLevelDTO.class,
      AccessLevelDTO.class,
      InfoLevelDTO.class,
      AssessmentLevelDTO.class
    })
@JsonSubTypes({
  @JsonSubTypes.Type(value = TrainingLevelDTO.class, name = "TrainingLevelDTO"),
  @JsonSubTypes.Type(value = AccessLevelDTO.class, name = "AccessLevelDTO"),
  @JsonSubTypes.Type(value = AssessmentLevelDTO.class, name = "AssessmentLevelDTO"),
  @JsonSubTypes.Type(value = InfoLevelDTO.class, name = "InfoLevelDTO")
})
public abstract class AbstractLevelDTO extends AbstractLevelBasicDTO {}
