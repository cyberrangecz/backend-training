package cz.cyberrange.platform.training.api.dto.export;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "JeopardyLevelExportDTO", description = "Exported Jeopardy level.", parent = AbstractLevelExportDTO.class)
public class JeopardyLevelExportDTO extends AbstractLevelExportDTO {

    @ApiModelProperty(value = "Categories with sublevels included in the level.")
    List<JeopardyCategoryExportDTO> categories;
}
