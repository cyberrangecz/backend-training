package cz.cyberrange.platform.training.api.dto.imports;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates information about assessment level. Inherits from {@link AbstractLevelImportDTO}
 */
@Getter
@Setter
@ToString
@ApiModel(value = "JeopardyLevelImportDTO", description = "Imported Jeopardy level.", parent = AbstractLevelImportDTO.class)
public class JeopardyLevelImportDTO extends AbstractLevelImportDTO {

    @ApiModelProperty(value = "Categories of Jeopardy level to import.")
    @NotNull(message = "{jeopardyLevel.categories.NotNull.message}")
    private List<JeopardyCategoryImportDTO> categories = new ArrayList<>();
}
