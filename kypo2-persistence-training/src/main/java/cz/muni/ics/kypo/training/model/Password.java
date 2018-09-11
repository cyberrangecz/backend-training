package cz.muni.ics.kypo.training.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Boris Jadus
 */
@Entity(name = "Password")
@Table(name = "password")
public class Password implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, insertable = false)
  private Long id;
  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  public Password(Long id, String passwordHash) {
    this.passwordHash = passwordHash;
    this.id = id;
  }

  public Password() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Password)) return false;
    Password keyword = (Password) o;
    return Objects.equals(id, keyword.id) &&
            Objects.equals(this.passwordHash, keyword.passwordHash);
  }

  @Override
  public int hashCode() {

    return Objects.hash(id, passwordHash);
  }

  @Override
  public String toString() {
    return "Keyword{" +
            "id=" + id +
            ", keywordHash=" + passwordHash +
            '}';
  }
}
