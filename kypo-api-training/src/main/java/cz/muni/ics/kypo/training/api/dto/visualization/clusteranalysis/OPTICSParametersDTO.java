package cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
@AllArgsConstructor
public class OPTICSParametersDTO {

    @ApiModelProperty(value = "Minimum number of points in a cluster", example = "5")
    private int minimumPoints;
    @ApiModelProperty(value = "Steepness threshold (xi)", example = "0.1")
    private double steepnessThreshold;
    @Nullable
    @ApiModelProperty(value = "Optional parameter helping reduce noise (epsilon)", example = "0.5")
    private Double noiseReduction;
}
