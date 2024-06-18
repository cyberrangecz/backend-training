package cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import cz.muni.ics.kypo.training.utils.ClusterMathUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.math3.stat.clustering.Clusterable;

import java.util.Collection;

/**
 * DTO for visualizing the clustering of time spent after hint was displayed and total wrong answers submitted after hint was displayed.
 */
@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class TimeAfterHintClusterableDTO implements Clusterable<TimeAfterHintClusterableDTO> {

    private final Long userRefId;

    private final Long level;

    private Double timeSpentAfterHint;

    private Double wrongFlagsAfterHint;

    private Double timeSpentAfterHintNormalized;

    private Double wrongFlagsAfterHintNormalized;

    public TimeAfterHintClusterableDTO(Long userRefId, Long level, Double timeSpentAfterHint, Double wrongFlagsAfterHint) {
        this.userRefId = userRefId;
        this.level = level;
        this.timeSpentAfterHint = timeSpentAfterHint;
        this.wrongFlagsAfterHint = wrongFlagsAfterHint;
        this.timeSpentAfterHintNormalized = timeSpentAfterHint;
        this.wrongFlagsAfterHintNormalized = wrongFlagsAfterHint;
    }

    @Override
    public double distanceFrom(TimeAfterHintClusterableDTO p) {
        return ClusterMathUtils.calculateDistance2D(timeSpentAfterHintNormalized, p.getTimeSpentAfterHintNormalized(), wrongFlagsAfterHintNormalized, p.getWrongFlagsAfterHintNormalized());
    }

    @Override
    public TimeAfterHintClusterableDTO centroidOf(Collection<TimeAfterHintClusterableDTO> p) {
        return new TimeAfterHintClusterableDTO(0L, 0L,
                computerTimeSpentAfterHint(p),
                computerWrongFlagsAfterHint(p));
    }

    private Double computerTimeSpentAfterHint(Collection<TimeAfterHintClusterableDTO> p) {
        return p.stream().reduce(
                0.0,
                (value, featureOne) -> value + featureOne.getTimeSpentAfterHintNormalized() / p.size(),
                Double::sum
        );
    }

    private Double computerWrongFlagsAfterHint(Collection<TimeAfterHintClusterableDTO> p) {
        return p.stream().reduce(
                0.0,
                (value, featureOne) -> value + featureOne.getWrongFlagsAfterHintNormalized() / p.size(),
                Double::sum
        );
    }
}
