package cz.cyberrange.platform.training.api.dto.imports;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "AttachmentImportDTO", description = "Imported attachment.")
public class AttachmentImportDTO {
    @ApiModelProperty(value = "URL link to file or website.")
    @NotEmpty(message = "{attachment.content.NotEmpty.message}")
    private String content;
}
