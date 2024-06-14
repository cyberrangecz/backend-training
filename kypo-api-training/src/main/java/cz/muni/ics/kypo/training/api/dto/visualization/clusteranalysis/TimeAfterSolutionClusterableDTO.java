package cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import cz.muni.ics.kypo.training.utils.ClusterMathUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.math3.stat.clustering.Clusterable;

import java.util.Collection;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class TimeAfterSolutionClusterableDTO implements Clusterable<TimeAfterSolutionClusterableDTO> {

    private final Long userRefId;

    private final Long level;

    private Double solutionDisplayedAt;

    private Double timeSpentAfterSolutionDisplayed;

    private Double solutionDisplayedAtNormalized;

    private Double timeSpentAfterSolutionDisplayedNormalized;

    public TimeAfterSolutionClusterableDTO(Long userRefId, Long level, Double solutionDisplayedAt, Double timeSpentAfterSolutionDisplayed) {
        this.userRefId = userRefId;
        this.level = level;
        this.solutionDisplayedAt = solutionDisplayedAt;
        this.timeSpentAfterSolutionDisplayed = timeSpentAfterSolutionDisplayed;
        this.solutionDisplayedAtNormalized = solutionDisplayedAt;
        this.timeSpentAfterSolutionDisplayedNormalized = timeSpentAfterSolutionDisplayed;
    }

    @Override
    public double distanceFrom(TimeAfterSolutionClusterableDTO p) {
        return ClusterMathUtils.calculateDistance2D(solutionDisplayedAtNormalized, p.getSolutionDisplayedAtNormalized(), timeSpentAfterSolutionDisplayedNormalized, p.getTimeSpentAfterSolutionDisplayedNormalized());
    }

    @Override
    public TimeAfterSolutionClusterableDTO centroidOf(Collection<TimeAfterSolutionClusterableDTO> p) {
        return new TimeAfterSolutionClusterableDTO(0L, 0L,
                computerSolutionDisplayedAt(p),
                computerTimeSpentAfterSolutionDisplayed(p));
    }
    
    private Double computerSolutionDisplayedAt(Collection<TimeAfterSolutionClusterableDTO> p) {
        return p.stream().reduce(
                0.0,
                (value, featureOne) -> value + featureOne.getSolutionDisplayedAtNormalized() / p.size(),
                Double::sum
        );
    }
    
    private Double computerTimeSpentAfterSolutionDisplayed(Collection<TimeAfterSolutionClusterableDTO> p) {
        return p.stream().reduce(
                0.0,
                (value, featureOne) -> value + featureOne.getTimeSpentAfterSolutionDisplayedNormalized() / p.size(),
                Double::sum
        );
    }
}
