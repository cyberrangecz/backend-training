package cz.muni.ics.kypo.training.api.dto;

public class AuthorRefDTO {
  private Long id;
  private String authorRefLogin;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAuthorRefLogin() {
    return authorRefLogin;
  }

  public void setAuthorRefLogin(String authorRefLogin) {
    this.authorRefLogin = authorRefLogin;
  }

  @Override
  public String toString() {
    return "AuthorRefDTO{" +
            "id=" + id +
            ", authorRefLogin='" + authorRefLogin + '\'' +
            '}';
  }
}
