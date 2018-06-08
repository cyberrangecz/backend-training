package cz.muni.ics.kypo.transfer;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
public class InfoDTO {

  private Long id;
  private String description;
  private String author;

  public InfoDTO() {}

  public InfoDTO(Long id, String description, String author) {
    super();
    this.id = id;
    this.description = description;
    this.author = author;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @Override
  public String toString() {
    return "InfoDTO [id=" + id + ", description=" + description + ", author=" + author + "]";
  }

}
