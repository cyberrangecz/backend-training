package cz.muni.ics.kypo.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity
@Table(name = "game_level")
public class GameLevel extends AbstractLevel {

  @Column(name = "flag", nullable = false)
  private String flag;
  @Column(name = "solution", nullable = false)
  private String solution;
  @Column(name = "content", nullable = false)
  private byte[] content;
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "gameLevel")
  private Set<Hint> hints = new HashSet<>(0);

  public GameLevel() {}

  public GameLevel(String flag, String solution, byte[] content, Set<Hint> hints) {
    super();
    this.flag = flag;
    this.solution = solution;
    this.content = content;
    this.hints = hints;
  }

  public String getFlag() {
    return flag;
  }

  public void setFlag(String flag) {
    this.flag = flag;
  }

  public String getSolution() {
    return solution;
  }

  public void setSolution(String solution) {
    this.solution = solution;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  public Set<Hint> getHints() {
    return Collections.unmodifiableSet(hints);
  }

  public void setHints(Set<Hint> hints) {
    this.hints = hints;
  }

  @Override
  public String toString() {
    return "GameLevel [flag=" + flag + ", solution=" + solution + ", content=" + content + ", hints=" + hints + "]";
  }

}
