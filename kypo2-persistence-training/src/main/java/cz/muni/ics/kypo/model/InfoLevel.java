package cz.muni.ics.kypo.model;

import java.util.Arrays;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity
@Table(catalog = "training", schema = "public", name = "info_level")
@PrimaryKeyJoinColumn(name = "id")
public class InfoLevel extends AbstractLevel {

  @Lob
  @Column(name = "content", nullable = false) // maybe should be unique?
  private byte[] content;

  public InfoLevel() {
    super();
  }

  public InfoLevel(byte[] content) {
    super();
    this.content = content;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
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
    return Arrays.equals(content, other.getContent());
  }

  @Override
  public String toString() {
    return "InfoLevel [content=" + Arrays.toString(content) + ", getId()=" + getId() + ", getTitle()=" + getTitle() + ", getMaxScore()=" + getMaxScore()
        + ", getOrder()=" + getOrder() + ", getPreHook()=" + Arrays.toString(getPreHook()) + ", getPostHook()=" + Arrays.toString(getPostHook())
        + ", getNextLevel()=" + getNextLevel() + ", getTrainingDefinition()=" + getTrainingDefinition() + ", getTrainingRun()=" + getTrainingRun()
        + ", toString()=" + super.toString() + "]";
  }

}
