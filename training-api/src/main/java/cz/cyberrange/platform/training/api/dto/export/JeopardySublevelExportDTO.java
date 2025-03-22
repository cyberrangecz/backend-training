package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "JeopardySublevelExportDTO", description = "Exported Jeopardy Sublevel.", parent = TrainingLevelExportDTO.class)
public class JeopardySublevelExportDTO extends TrainingLevelExportDTO {

    @ApiModelProperty(value = "Short description of the sublevel.")
    String description;

}
