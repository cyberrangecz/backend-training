package cz.muni.ics.kypo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity
@Table(name = "hint")
public class Hint {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "title", nullable = false)
  private String title;
  @Column(name = "content", nullable = false)
  private byte[] content;
  @Column(name = "points", nullable = false)
  private Integer points;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hints")
  private GameLevel gameLevel;

  public Hint() {}

  public Hint(Long id, String title, byte[] content, Integer points, GameLevel gameLevel) {
    super();
    this.id = id;
    this.title = title;
    this.content = content;
    this.points = points;
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

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  public Integer getPoints() {
    return points;
  }

  public void setPoints(Integer points) {
    this.points = points;
  }

  public GameLevel getGameLevel() {
    return gameLevel;
  }

  public void setGameLevel(GameLevel gameLevel) {
    this.gameLevel = gameLevel;
  }

  @Override
  public String toString() {
    return "Hint [id=" + id + ", title=" + title + ", content=" + content + ", points=" + points + ", gameLevel=" + gameLevel + "]";
  }

}
