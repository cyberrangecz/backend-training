package cz.muni.ics.kypo.training.model;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "ParticipantRef")
@Table(name = "participant_ref")
public class ParticipantRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Column(name = "participant_ref_login", nullable = false)
    private String participantRefLogin;

    public ParticipantRef() {};

    public ParticipantRef(String login) {
        this.participantRefLogin = login;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParticipantRefLogin() {
        return participantRefLogin;
    }

    public void setParticipantRefLogin(String participantRefLogin) {
        this.participantRefLogin = participantRefLogin;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof ParticipantRef))
            return false;

        ParticipantRef other = (ParticipantRef) obj;
        return Objects.equals(id, other.getId())
                && Objects.equals(participantRefLogin, other.getParticipantRefLogin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, participantRefLogin);
    }

    @Override
    public String toString() {
        return "ParticipantRef{" +
                "id=" + id +
                ", participantRefLogin=" + participantRefLogin +
                '}';
    }
}
