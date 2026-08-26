package cz.cyberrange.platform.training.api.dto.accesslevel;

import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** A level whose content instructs the participant how to connect to their virtual machines. */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "AccessLevelDTO",
    description = "A level containing instructions on how to connect to the virtual machines.",
    parent = AbstractLevelDTO.class)
public class AccessLevelDTO extends AbstractLevelDTO {

  @ApiModelProperty(
      value = "Keyword found in training, used for access next level.",
      example = "secretAnswer")
  private String passkey;

  @ApiModelProperty(
      value = "The instructions on how to connect to the machine in cloud environment.",
      example = "Connect using SSH config.")
  private String cloudContent;

  @ApiModelProperty(
      value = "The instructions on how to connect to the machine in local (non-cloud) environment.",
      example = "Use vagrant SSH connection.")
  private String localContent;
}
