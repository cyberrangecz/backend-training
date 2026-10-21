package cz.cyberrange.platform.training.api.dto.traininglevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The content of a training level plus the id of the stored level it replaces. Carries the
 * discriminator that lets {@link AbstractLevelUpdateDTO} resolve this subtype during
 * deserialization.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "The content stored on a training level, with the level's id.")
public class TrainingLevelUpdateDTO extends TrainingLevelContentDTO
    implements AbstractLevelUpdateDTO {

  @Schema(
      description = "Identifies the stored level to replace; leave it out to create a new level.",
      example = "1")
  private Long id;
}
