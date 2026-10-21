package cz.cyberrange.platform.training.api.dto.infolevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelContentDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Holds the content of an info level in a request. Extended by {@link InfoLevelCreateDTO} and
 * {@link InfoLevelUpdateDTO}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class InfoLevelContentDTO extends AbstractLevelContentDTO {

  /** Text the level presents to the participant */
  @Schema(
      description = "Text the level presents to the participant.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "Informational stuff")
  @NotEmpty(message = "{infoLevel.content.NotEmpty.message}")
  private String content;

  /** Sets the level type discriminator to info level */
  protected InfoLevelContentDTO() {
    this.levelType = LevelType.INFO_LEVEL;
  }
}
