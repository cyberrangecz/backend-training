package cz.muni.ics.kypo.training.model;

import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity(name = "Hint")
@Table(name = "hint")
public class Hint implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, insertable = false)
  private Long id;
  @Column(name = "title", nullable = false)
  private String title;
  @Lob
  @Type(type = "org.hibernate.type.StringType")
  @Column(name = "content", nullable = false)
  private String content;
  @Column(name = "hint_penalty", nullable = false)
  private Integer hintPenalty;
  @ManyToOne(fetch = FetchType.LAZY)
  private GameLevel gameLevel;

  public Hint() {}

  public Hint(Long id, String title, String content, Integer hintPenalty, GameLevel gameLevel) {
    super();
    this.id = id;
    this.title = title;
    this.content = content;
    this.hintPenalty = hintPenalty;
    this.gameLevel = gameLevel;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Integer getHintPenalty() {
    return hintPenalty;
  }

  public void setHintPenalty(Integer hintPenalty) {
    this.hintPenalty = hintPenalty;
  }

  public GameLevel getGameLevel() {
    return gameLevel;
  }

  public void setGameLevel(GameLevel gameLevel) {
    this.gameLevel = gameLevel;
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, gameLevel, hintPenalty, title);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof Hint))
      return false;
    Hint other = (Hint) obj;
    // @formatter:off
    return Objects.equals(content, other.getContent())
        && Objects.equals(gameLevel, other.getGameLevel()) 
        && Objects.equals(hintPenalty, other.getHintPenalty())
        && Objects.equals(title, other.getTitle());
    // @formatter:on
  }

  @Override
  public String toString() {
    return "Hint [id=" + id + ", title=" + title + ", content=" + content + ", hintPenalty=" + hintPenalty + ", gameLevel=" + gameLevel + ", toString()="
        + super.toString() + "]";
  }

}
