package cz.muni.ics.kypo.training.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Boris Jadus
 */
@Entity(name = "Keyword")
@Table(name = "keyword")
public class Keyword implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, insertable = false)
  private Long id;
  @Column(name = "keyword_hash", nullable = false)
  private String keywordHash;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getKeywordHash() {
    return keywordHash;
  }

  public void setKeywordHash(String keywordHash) {
    this.keywordHash = keywordHash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Keyword)) return false;
    Keyword keyword = (Keyword) o;
    return Objects.equals(id, keyword.id) &&
            Objects.equals(this.keywordHash, keyword.keywordHash);
  }

  @Override
  public int hashCode() {

    return Objects.hash(id, keywordHash);
  }

  @Override
  public String toString() {
    return "Keyword{" +
            "id=" + id +
            ", keywordHash=" + keywordHash +
            '}';
  }
}
