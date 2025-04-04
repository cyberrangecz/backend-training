package cz.cyberrange.platform.training.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Embeddable
public class TrainingInstanceLobby implements Serializable {

    public static final int TEAM_SIZE_LIMIT = 12;

    @Setter
    @OneToOne
    @JoinColumn(name = "training_instance_id")
    private TrainingInstance trainingInstance;

    @ManyToMany
    @JoinTable(
            name = "training_instance_waiting_users",
            joinColumns = @JoinColumn(name = "training_instance_id"),
            inverseJoinColumns = @JoinColumn(name = "user_ref_id")
    )
    private Set<UserRef> usersQueue = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "training_instance_id")
    private Set<Team> teams = new HashSet<>();

    public void addWaitingUser(UserRef userRef) {
        usersQueue.add(userRef);
        userRef.addQueue(trainingInstance);
    }

    public void removeWaitingUser(UserRef userRef) {
        usersQueue.remove(userRef);
        userRef.removeQueue(trainingInstance);
    }

    public void addTeam(Team team) {
        teams.add(team);
    }

    public void removeTeam(Team team) {
        teams.remove(team);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingInstanceLobby that = (TrainingInstanceLobby) o;
        return this.hashCode() == that.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Objects.hashCode(trainingInstance),
                Arrays.deepHashCode(usersQueue.toArray()),
                Arrays.deepHashCode(teams.toArray()));
    }
}
