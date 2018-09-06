package cz.muni.ics.kypo.training.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "ParticipantRef")
@Table(name = "participant_ref")
public class ParticipantRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @Column(name = "participant_ref")
    private Long participantRefId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParticipantRefId() {
        return participantRefId;
    }

    public void setParticipantRefId(Long participantRefId) {
        this.participantRefId = participantRefId;
    }

    public ParticipantRef() {
    }

    public ParticipantRef(long participantRefId) {
        this.participantRefId = participantRefId;
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
                && Objects.equals(participantRefId, other.getParticipantRefId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, participantRefId);
    }

    @Override
    public String toString() {
        return "ParticipantRef{" +
                "id=" + id +
                ", participantRefId=" + participantRefId +
                '}';
    }
}
