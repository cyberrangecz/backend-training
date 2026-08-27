package cz.cyberrange.platform.training.persistence.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
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
  @Column(name = "id", unique = true, nullable = false, insertable = false)
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
}
