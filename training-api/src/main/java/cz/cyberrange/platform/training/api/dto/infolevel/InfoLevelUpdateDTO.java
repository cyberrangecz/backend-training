package cz.cyberrange.platform.training.api.dto.infolevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Payload for replacing an info level's content. Carries the {@code INFO_LEVEL} discriminator that
 * lets {@link AbstractLevelUpdateDTO} resolve this subtype during deserialization.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "The text that replaces what an info level currently presents.")
public class InfoLevelUpdateDTO extends AbstractLevelUpdateDTO {

  /** Text that replaces the level's stored content verbatim when the update is applied */
  @Schema(
      description = "Text that replaces the level's current content.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "Informational stuff")
  @NotEmpty(message = "{infoLevel.content.NotEmpty.message}")
  private String content;

  /** Sets the level type discriminator to info level */
  public InfoLevelUpdateDTO() {
    this.levelType = LevelType.INFO_LEVEL;
  }
}
