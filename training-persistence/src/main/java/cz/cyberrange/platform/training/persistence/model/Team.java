package cz.cyberrange.platform.training.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
@Table(name = "team")
public class Team extends AbstractEntity<Long> {

    @Setter
    @Getter
    @Column(name = "name")
    private String name;

    @Getter
    @Setter
    @ManyToOne
    private TrainingInstance trainingInstance;

    @Setter
    @Getter
    @Column(name = "locked")
    private boolean locked;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "team_user",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "user_ref_id")
    )
    private Set<UserRef> members = new HashSet<>();


    public Set<UserRef> getMembers() {
        return members;
    }

    public void addMember(UserRef userRef) {
        userRef.addToTeam(this);
        members.add(userRef);
    }

    public void removeMember(UserRef userRef) {
        userRef.removeFromTeam(this);
        members.remove(userRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Arrays.deepHashCode(members.toArray()));
    }
}
