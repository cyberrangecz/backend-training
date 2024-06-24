package cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis;

import cz.muni.ics.kypo.training.utils.ClusterMathUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.math3.stat.clustering.Clusterable;

import java.util.Collection;

/**
 * DTO for visualizing the clustering of
 * wrong answers submitted and time played.
 */
@Data
@AllArgsConstructor
public class WrongAnswersClusterableDTO implements Clusterable<WrongAnswersClusterableDTO> {

    private final Long userRefId;
    private Double wrongAnswersSubmitted;
    private Double timePlayed;
    private Double wrongAnswersSubmittedNormalized;
    private Double timePlayedNormalized;

    public WrongAnswersClusterableDTO(Long userRefId, Double wrongAnswersSubmitted, Double timePlayed) {
        this.userRefId = userRefId;
        this.wrongAnswersSubmitted = wrongAnswersSubmitted;
        this.timePlayed = timePlayed;
        this.wrongAnswersSubmittedNormalized = wrongAnswersSubmitted;
        this.timePlayedNormalized = timePlayed;
    }


    @Override
    public double distanceFrom(WrongAnswersClusterableDTO otherClusterable) {
        return ClusterMathUtils.calculateDistance2D(
                wrongAnswersSubmittedNormalized, otherClusterable.getWrongAnswersSubmittedNormalized(),
                timePlayedNormalized, otherClusterable.getTimePlayedNormalized()
        );
    }

    @Override
    public WrongAnswersClusterableDTO centroidOf(Collection<WrongAnswersClusterableDTO> clusterables) {
        return new WrongAnswersClusterableDTO(0L,
                computerWrongAnswersSubmitted(clusterables),
                computeTimePlayed(clusterables));
    }

    private Double computerWrongAnswersSubmitted(Collection<WrongAnswersClusterableDTO> clusterables) {
        return clusterables.stream().reduce(
                0.0,
                (accumulator, newValue) -> accumulator + newValue.getWrongAnswersSubmittedNormalized() / clusterables.size(),
                Double::sum
        );
    }

    private Double computeTimePlayed(Collection<WrongAnswersClusterableDTO> clusterables) {
        return clusterables.stream().reduce(
                0.0,
                (accumulator, newValue) -> accumulator + newValue.getTimePlayedNormalized() / clusterables.size(),
                Double::sum
        );
    }

}

