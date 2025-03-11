package cz.cyberrange.platform.training.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Class representing hints associated with training level that can be displayed by trainee if they are in need of help with
 * solving given level
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hint")
@NamedQueries({
        @NamedQuery(
                name = "Hint.deleteHintsByLevelId",
                query = "DELETE FROM Hint h WHERE h.trainingLevel.id = :levelId"
        ),
})
public class Hint extends AbstractEntity<Long> {

    @Column(name = "title", nullable = false)
    private String title;
    @Lob
    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "hint_penalty", nullable = false)
    private Integer hintPenalty;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_level_id")
    private TrainingLevel trainingLevel;
    @Column(name = "order_in_level", nullable = false)
    private int order;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Hint hint = (Hint) o;
        return getId() != null && Objects.equals(getId(), hint.getId());
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(this.getId());
    }
}
