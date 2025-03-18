package cz.cyberrange.platform.training.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Table(name = "team")
public class Team extends AbstractEntity<Long> {

    @ManyToOne(cascade = CascadeType.PERSIST)
    @Setter
    private TrainingInstance trainingInstance;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "team_user",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "user_ref_id")
    )
    private Set<UserRef> userRefs = new HashSet<>();

    @Override
    public int hashCode() {
        return Objects.hash(
                Objects.hashCode(trainingInstance),
                Arrays.deepHashCode(userRefs.toArray()));
    }
}
