package cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis;

import cz.muni.ics.kypo.training.utils.ClusterMathUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.math3.stat.clustering.Clusterable;

import java.util.Collection;

/**
 * DTO for visualizing the clustering of wrong answers submitted and time played.
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
    public double distanceFrom(WrongAnswersClusterableDTO p) {
        return ClusterMathUtils.calculateDistance2D(wrongAnswersSubmittedNormalized, p.getWrongAnswersSubmittedNormalized(), timePlayedNormalized, p.getTimePlayedNormalized());
    }

    @Override
    public WrongAnswersClusterableDTO centroidOf(Collection<WrongAnswersClusterableDTO> p) {
        return new WrongAnswersClusterableDTO(0L,
                computerWrongAnswersSubmitted(p),
                computeTimePlayed(p));
    }

    private Double computerWrongAnswersSubmitted(Collection<WrongAnswersClusterableDTO> p) {
        return p.stream().reduce(
                0.0,
                (value, featureOne) -> value + featureOne.getWrongAnswersSubmittedNormalized() / p.size(),
                Double::sum
        );
    }

    private Double computeTimePlayed(Collection<WrongAnswersClusterableDTO> p) {
        return p.stream().reduce(
                0.0,
                (value, featureOne) -> value + featureOne.getTimePlayedNormalized() / p.size(),
                Double::sum
        );
    }

}

