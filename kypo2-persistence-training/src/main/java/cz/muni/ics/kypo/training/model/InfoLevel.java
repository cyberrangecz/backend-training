package cz.muni.ics.kypo.training.model;

import org.hibernate.annotations.Type;

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
  @Type(type = "org.hibernate.type.StringType")
  @Column(name = "content", nullable = false) // maybe should be unique?
  private String content;

  public InfoLevel() {
    super();
  }

  public InfoLevel(String content) {
    super();
    this.content = content;
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
    return "InfoLevel [content=" + content + ", getId()=" + getId() + ", getTitle()=" + getTitle() + ", getMaxScore()="
        + getMaxScore()  + ", getPreHook()=" + getPreHook() + ", getPostHook()=" + getPostHook() + ", getNextLevel()=" + getNextLevel()
        + ", toString()=" + super.toString() + "]";
  }

}
