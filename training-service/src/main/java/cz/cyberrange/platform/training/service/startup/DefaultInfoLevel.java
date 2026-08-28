package cz.cyberrange.platform.training.service.startup;

import javax.validation.constraints.NotEmpty;

/**
 * The title and content a newly created info level starts out with. Both must be present and
 * non-empty for the content to load.
 */
public class DefaultInfoLevel {
  @NotEmpty(message = "{abstractLevel.title.NotEmpty.message}")
  private String title;

  @NotEmpty(message = "{infoLevel.content.NotEmpty.message}")
  private String content;

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
}
