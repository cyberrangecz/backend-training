package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Pavel Seda (441048)
 */
@Entity(name = "PostHook")
@Table(name = "post_hook")
public class PostHook implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
    @OneToOne(mappedBy = "postHook", fetch = FetchType.LAZY)
    private AbstractLevel abstractLevel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AbstractLevel getAbstractLevel() {
        return abstractLevel;
    }

    public void setAbstractLevel(AbstractLevel abstractLevel) {
        this.abstractLevel = abstractLevel;
    }

    @Override
    public String toString() {
        return "PostHook{" +
                "id=" + id +
                ", abstractLevel=" + abstractLevel +
                '}';
    }
}
