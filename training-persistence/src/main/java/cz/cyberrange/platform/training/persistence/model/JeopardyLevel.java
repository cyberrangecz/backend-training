package cz.cyberrange.platform.training.persistence.model;

import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.List;

/**
 * Class specifying Abstract level as training level.
 * Training levels contain tasks for trainees to solve.
 */
@ToString
@Entity
@Table(name = "jeopardy_level")
@PrimaryKeyJoinColumn(name = "id")
public class JeopardyLevel extends AbstractLevel {

    @OneToMany(cascade = CascadeType.ALL)
    List<TrainingLevel> trainingLevels;

    @Override
    public boolean equals(Object o) {
        return o instanceof JeopardyLevel other &&
                Arrays.deepEquals(
                        trainingLevels.toArray(),
                        other.trainingLevels.toArray()
                );
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(trainingLevels.toArray());
    }
}
