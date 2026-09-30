package cz.cyberrange.platform.training.api.dto.infolevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Full representation of an info level, carrying its content. Reaches both a designer, organizer or
 * administrator viewing the level and a participant currently on it, since the content holds
 * nothing that must be kept from either.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "An info level together with the text it presents to the participant.")
public class InfoLevelDTO extends AbstractLevelDTO {

  @Schema(
      description = "The text the level presents to the participant.",
      example = "Informational stuff")
  private String content;
}
