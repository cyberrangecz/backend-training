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
@ApiModel(value = "JeopardyCategoryExportDTO", description = "Exported Jeopardy Category.")
public class JeopardyCategoryExportDTO {

    @ApiModelProperty(value = "The title of the category.")
    private String title;

    @ApiModelProperty(value = "The color of the category.")
    private Integer color;

    @ApiModelProperty(value = "Sublevels included in the category.")
    private List<JeopardySublevelExportDTO> sublevels;

}
