package cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import cz.muni.ics.kypo.training.utils.ClusterMathUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.math3.stat.clustering.Clusterable;

import java.util.Collection;

/**
 * DTO for visualizing the clustering of time spent after a
 * hint was displayed and total wrong answers submitted after a
 * hint was displayed.
 */
@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class TimeAfterHintClusterableDTO implements Clusterable<TimeAfterHintClusterableDTO> {

    private final Long userRefId;
    private final Long level;
    private Double timeSpentAfterHint;
    private Double wrongAnswersAfterHint;
    private Double timeSpentAfterHintNormalized;
    private Double wrongAnswersAfterHintNormalized;

    public TimeAfterHintClusterableDTO(Long userRefId, Long level,
                                       Double timeSpentAfterHint, Double wrongAnswersAfterHint) {
        this.userRefId = userRefId;
        this.level = level;
        this.timeSpentAfterHint = timeSpentAfterHint;
        this.wrongAnswersAfterHint = wrongAnswersAfterHint;
        this.timeSpentAfterHintNormalized = timeSpentAfterHint;
        this.wrongAnswersAfterHintNormalized = wrongAnswersAfterHint;
    }

    @Override
    public double distanceFrom(TimeAfterHintClusterableDTO otherClusterable) {
        return ClusterMathUtils.calculateDistance2D(
                timeSpentAfterHintNormalized, otherClusterable.getTimeSpentAfterHintNormalized(),
                wrongAnswersAfterHintNormalized, otherClusterable.getWrongAnswersAfterHintNormalized()
        );
    }

    @Override
    public TimeAfterHintClusterableDTO centroidOf(Collection<TimeAfterHintClusterableDTO> clusterables) {
        return new TimeAfterHintClusterableDTO(0L, 0L,
                computerTimeSpentAfterHint(clusterables),
                computerWrongAnswersAfterHint(clusterables));
    }

    private Double computerTimeSpentAfterHint(Collection<TimeAfterHintClusterableDTO> clusterables) {
        return clusterables.stream().reduce(
                0.0,
                (accumulator, newValue) -> accumulator + newValue.getTimeSpentAfterHintNormalized() / clusterables.size(),
                Double::sum
        );
    }

    private Double computerWrongAnswersAfterHint(Collection<TimeAfterHintClusterableDTO> clusterables) {
        return clusterables.stream().reduce(
                0.0,
                (accumulator, newValue) -> accumulator + newValue.getWrongAnswersAfterHintNormalized() / clusterables.size(),
                Double::sum
        );
    }
}
