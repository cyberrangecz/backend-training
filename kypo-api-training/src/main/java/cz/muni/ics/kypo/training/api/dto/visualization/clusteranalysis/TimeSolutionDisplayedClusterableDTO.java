package cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import cz.muni.ics.kypo.training.utils.ClusterMathUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.math3.stat.clustering.Clusterable;

import java.util.Collection;

/**
 * DTO for visualizing the clustering of time when a
 * solution was displayed and time spent after a
 * solution was displayed.
 */
@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class TimeSolutionDisplayedClusterableDTO implements Clusterable<TimeSolutionDisplayedClusterableDTO> {

    private final Long userRefId;
    private final Long level;
    private Double solutionDisplayedAt;
    private Double timeSpentAfterSolutionDisplayed;
    private Double solutionDisplayedAtNormalized;
    private Double timeSpentAfterSolutionDisplayedNormalized;

    public TimeSolutionDisplayedClusterableDTO(Long userRefId, Long level,
                                               Double solutionDisplayedAt, Double timeSpentAfterSolutionDisplayed) {
        this.userRefId = userRefId;
        this.level = level;
        this.solutionDisplayedAt = solutionDisplayedAt;
        this.timeSpentAfterSolutionDisplayed = timeSpentAfterSolutionDisplayed;
        this.solutionDisplayedAtNormalized = solutionDisplayedAt;
        this.timeSpentAfterSolutionDisplayedNormalized = timeSpentAfterSolutionDisplayed;
    }

    @Override
    public double distanceFrom(TimeSolutionDisplayedClusterableDTO otherClusterable) {
        return ClusterMathUtils.calculateDistance2D(
                solutionDisplayedAtNormalized, otherClusterable.getSolutionDisplayedAtNormalized(),
                timeSpentAfterSolutionDisplayedNormalized, otherClusterable.getTimeSpentAfterSolutionDisplayedNormalized()
        );
    }

    @Override
    public TimeSolutionDisplayedClusterableDTO centroidOf(Collection<TimeSolutionDisplayedClusterableDTO> clusterables) {
        return new TimeSolutionDisplayedClusterableDTO(0L, 0L,
                computerSolutionDisplayedAt(clusterables),
                computerTimeSpentAfterSolutionDisplayed(clusterables));
    }

    private Double computerSolutionDisplayedAt(Collection<TimeSolutionDisplayedClusterableDTO> clusterables) {
        return clusterables.stream().reduce(
                0.0,
                (accumulator, newValue) -> accumulator + newValue.getSolutionDisplayedAtNormalized() / clusterables.size(),
                Double::sum
        );
    }

    private Double computerTimeSpentAfterSolutionDisplayed(Collection<TimeSolutionDisplayedClusterableDTO> clusterables) {
        return clusterables.stream().reduce(
                0.0,
                (accumulator, newValue) -> accumulator + newValue.getTimeSpentAfterSolutionDisplayedNormalized() / clusterables.size(),
                Double::sum
        );
    }
}
