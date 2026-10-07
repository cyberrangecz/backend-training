package cz.cyberrange.platform.training.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Base shared by every persisted entity. Supplies the generated identity column and defines entity
 * equality by that column rather than by any other field.
 *
 * @param <PK> the type of the identity column.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@MappedSuperclass
public class AbstractEntity<PK extends Serializable> implements Serializable {

  // Assigned by the database on insert; never supplied by the application.
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private PK id;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof AbstractEntity other) {
      return id != null && id.equals(other.id);
    }
    return false;
  }

  /**
   * Returns one hash shared by every entity, so the hash never changes when the database assigns
   * the identity column and stays equal for entities and their proxies that compare equal.
   *
   * @return the hash shared by every entity
   */
  @Override
  public int hashCode() {
    return AbstractEntity.class.hashCode();
  }
}
