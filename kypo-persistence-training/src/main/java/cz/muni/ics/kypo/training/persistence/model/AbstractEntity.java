package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The type Abstract entity.
 *
 * @param <PK> Primary key for a given entity.
 */
@MappedSuperclass
public class AbstractEntity<PK extends Serializable> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private PK id;

    /**
     * Instantiates a new Abstract entity.
     */
    public AbstractEntity() {
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public PK getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(PK id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "AbstractEntity{" +
                "id=" + id +
                '}';
    }
}
