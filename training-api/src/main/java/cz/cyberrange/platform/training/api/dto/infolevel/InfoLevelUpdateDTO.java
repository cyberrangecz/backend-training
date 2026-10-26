package cz.cyberrange.platform.training.api.dto.infolevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The content of an info level plus the id of the stored level it replaces. Carries the
 * discriminator that lets {@link AbstractLevelUpdateDTO} resolve this subtype during
 * deserialization.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "The content stored on an info level, with the level's id.")
public class InfoLevelUpdateDTO extends InfoLevelContentDTO implements AbstractLevelUpdateDTO {

  @Schema(
      description = "Identifies the stored level to replace; leave it out to create a new level.",
      example = "1")
  private Long id;
}
