package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class representing mitre technique used in Training Level.
 */
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

    /**
     * Instantiates a new Mitre Technique
     */
    public MitreTechnique() {
    }

    /**
     * Instantiates a new Mitre Technique
     *
     * @param id          unique identification number of mitre technique
     * @param techniqueKey string representing unique key of the technique
     */
    public MitreTechnique(Long id, String techniqueKey) {
        this.techniqueKey = techniqueKey;
        super.setId(id);
    }

    /**
     * Gets unique identification number of Mitre technique
     *
     * @return the id
     */
    public Long getId() {
        return super.getId();
    }

    /**
     * Sets unique identification number of Mitre technique
     *
     * @param id the id
     */
    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * Gets string representing unique key of the technique
     *
     * @return the Mitre technique
     */
    public String getTechniqueKey() {
        return techniqueKey;
    }

    /**
     * Sets string representing unique key of the technique
     *
     * @param techniqueKey the Mitre technique
     */
    public void setTechniqueKey(String techniqueKey) {
        this.techniqueKey = techniqueKey;
    }

    /**
     * Gets set of training levels using the technique
     *
     * @return set of training levels
     */
    public Set<TrainingLevel> getTrainingLevels() {
        return trainingLevels;
    }

    /**
     * Sets set of training levels using the technique
     *
     * @param trainingLevels set of training levels
     */
    public void setTrainingLevels(Set<TrainingLevel> trainingLevels) {
        this.trainingLevels = trainingLevels;
    }


    public void addTrainingLevel(TrainingLevel trainingLevel) {
        this.trainingLevels.add(trainingLevel);
    }

    public void removeTrainingLevel(TrainingLevel trainingLevel) {
        this.trainingLevels.remove(trainingLevel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MitreTechnique))
            return false;
        MitreTechnique accessToken = (MitreTechnique) o;
        return Objects.equals(this.techniqueKey, accessToken.getTechniqueKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(techniqueKey);
    }

    @Override
    public String toString() {
        return "MitreTechnique{" +
                "id=" + super.getId() +
                ", techniqueKey='" + techniqueKey + '\'' +
                '}';
    }
}
