package cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
@AllArgsConstructor
public class OPTICSParameters {

    @ApiModelProperty(value = "Minimum number of points in a cluster", example = "5")
    private int minPts;

    @ApiModelProperty(value = "Steepness threshold", example = "0.1")
    private double xi;

    @Nullable
    @ApiModelProperty(value = "Optional parameter helping reduce noise", example = "0.5")
    private Double epsilon;
}
