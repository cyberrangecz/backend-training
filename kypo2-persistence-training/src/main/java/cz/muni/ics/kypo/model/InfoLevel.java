package cz.muni.ics.kypo.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Entity
@Table(name = "info_level")
public class InfoLevel extends AbstractLevel {

  @Column(name = "content", nullable = false)
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
  public String toString() {
    return "InfoLevel [content=" + content + "]";
  }

}
