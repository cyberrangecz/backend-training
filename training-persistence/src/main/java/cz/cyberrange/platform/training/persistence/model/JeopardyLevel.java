package cz.cyberrange.platform.training.persistence.model;

import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.ArrayList;
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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "jeopardyLevel")
    List<JeopardyCategory> categories = new ArrayList<>();

    public void setCategories(List<JeopardyCategory> categories) {
        this.categories = new ArrayList<>(categories);
    }

    public void addCategory(JeopardyCategory category) {
        categories.add(category);
    }

    /**
     * @return copy of categories list
     */
    public List<JeopardyCategory> getCategories() {
        return new ArrayList<>(categories);
    }

    public long sumSublevelsTotalDuration() {
        return this.getCategories().stream().mapToLong(
                category -> category.getSublevels().stream().mapToLong(JeopardySublevel::getEstimatedDuration).sum()
        ).sum();
    }

    public int sumSublevelsTotalScore() {
        return this.getCategories().stream().mapToInt(
                category -> category.getSublevels().stream().mapToInt(JeopardySublevel::getMaxScore).sum()
        ).sum();
    }

    public List<JeopardySublevel> flatCopyOfSublevels() {
        return this.categories.stream().flatMap(category -> category.getSublevels().stream()).toList();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof JeopardyLevel other &&
                Arrays.deepEquals(
                        categories.toArray(),
                        other.categories.toArray()
                );
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(categories.toArray());
    }
}
