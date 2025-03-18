package cz.cyberrange.platform.training.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Entity
@Table(name = "coop_instance_queue")
public class InstanceQueue extends AbstractEntity<Long> {

    @Setter
    @OneToOne()
    private TrainingInstance trainingInstance;

    @OneToMany
    private Set<UserRef> waitingUsers = new HashSet<>();

    @OneToMany
    private Set<Team> teams = new HashSet<>();

    public void setWaitingUsers(Set<UserRef> queue) {
        this.waitingUsers = Collections.unmodifiableSet(queue);
    }

    public void setTeams(Set<Team> queue) {
        this.teams = Collections.unmodifiableSet(queue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceQueue that = (InstanceQueue) o;
        return this.hashCode() == that.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Objects.hashCode(trainingInstance),
                Arrays.deepHashCode(waitingUsers.toArray()),
                Arrays.deepHashCode(teams.toArray()));
    }
}
