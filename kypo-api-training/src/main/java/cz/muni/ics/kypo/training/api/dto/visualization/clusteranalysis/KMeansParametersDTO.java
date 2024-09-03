package cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KMeansParametersDTO {

    @ApiModelProperty(value = "Number of clusters to create.", example = "3")
    private int numberOfClusters;
}
