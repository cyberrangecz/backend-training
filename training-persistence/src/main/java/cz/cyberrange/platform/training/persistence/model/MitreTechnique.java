package cz.cyberrange.platform.training.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * Class representing mitre technique used in Training Level.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mitre_technique")
@NamedQueries({
        @NamedQuery(
                name = "MitreTechnique.findByTechniqueKey",
                query = "SELECT mt FROM MitreTechnique mt WHERE mt.techniqueKey = :techniqueKey"
        ),
})
public class MitreTechnique extends AbstractEntity<Long> {

    @ManyToMany(mappedBy = "mitreTechniques")
    private Set<TrainingLevel> trainingLevels = new HashSet<>();
    @Column(name = "technique_key", nullable = false, unique = true)
    private String techniqueKey;

    public void addTrainingLevel(TrainingLevel trainingLevel) {
        this.trainingLevels.add(trainingLevel);
    }

    public void removeTrainingLevel(TrainingLevel trainingLevel) {
        this.trainingLevels.remove(trainingLevel);
    }
}
