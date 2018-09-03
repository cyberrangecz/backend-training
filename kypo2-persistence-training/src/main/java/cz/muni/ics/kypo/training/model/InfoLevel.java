package cz.muni.ics.kypo.training.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity(name = "InfoLevel")
@Table(name = "info_level")
@PrimaryKeyJoinColumn(name = "id")
public class InfoLevel extends AbstractLevel implements Serializable {

  @Lob
  @Column(name = "content", unique = true, nullable = false) // maybe should be unique?
  private String content;

  public InfoLevel() {
    super();
  }

  public InfoLevel(String content) {
    super();
    this.content = content;
  }

  public Long getId() {
    return super.getId();
  }

  public void setId(Long id) {
    super.setId(id);
  }

  public String getTitle() {
    return super.getTitle();
  }

  public void setTitle(String title) {
    super.setTitle(title);
  }

  public int getMaxScore() {
    return super.getMaxScore();
  }

  public void setMaxScore(int maxScore) {
    super.setMaxScore(maxScore);
  }

  public Long getLevelOrder() {
    return super.getLevelOrder();
  }

  public void setLevelOrder(Long levelOrder) {
    super.setLevelOrder(levelOrder);
  }

  public PreHook getPreHook() {
    return super.getPreHook();
  }

  public void setPreHook(PreHook preHook) {
    super.setPreHook(preHook);
  }

  public PostHook getPostHook() {
    return super.getPostHook();
  }

  public void setPostHook(PostHook postHook) {
    super.setPostHook(postHook);
  }

  public Long getNextLevel() {
    return super.getNextLevel();
  }

  public void setNextLevel(Long nextLevel) {
    super.setNextLevel(nextLevel);
  }

  public TrainingDefinition getTrainingDefinition() {
    return super.getTrainingDefinition();
  }

  public void setTrainingDefinition(TrainingDefinition trainingDefinition) {
    super.setTrainingDefinition(trainingDefinition);
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(content);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (!(obj instanceof InfoLevel))
      return false;
    InfoLevel other = (InfoLevel) obj;
    return Objects.equals(content, other.getContent());
  }

  @Override
  public String toString() {
    return "InfoLevel [content=" + content + ", getId()=" + getId() + ", getTitle()=" + getTitle() + ", getMaxScore()=" + getMaxScore() + ", getLevelOrder()="
        + getLevelOrder() + ", getPreHook()=" + getPreHook() + ", getPostHook()=" + getPostHook() + ", getNextLevel()=" + getNextLevel()
        + ", getTrainingDefinition()=" + getTrainingDefinition() + ", toString()=" + super.toString() + "]";
  }

}
