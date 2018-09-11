package cz.muni.ics.kypo.training.api.dto;

import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import io.swagger.annotations.ApiModel;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
@ApiModel(value = "AbstractLevelDTO", description = ".")
public abstract class AbstractLevelDTO {
  protected Long id;
  @NotEmpty(message = "Level title cannot be empty")
  protected String title;
  @NotNull
  @Min(value = 0, message = "Max score cannot be lower than 0")
  @Max(value = 100, message = "Max score cannot be greater than 100")
  protected int maxScore;
  protected Long nextLevel;
  protected PreHookDTO preHook;
  protected PostHookDTO postHook;

  public AbstractLevelDTO() {}

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

  public int getMaxScore() {
    return maxScore;
  }

  public void setMaxScore(int maxScore) {
    this.maxScore = maxScore;
  }

  public Long getNextLevel() {
    return nextLevel;
  }

  public void setNextLevel(Long nextLevel) {
    this.nextLevel = nextLevel;
  }

  public PreHookDTO getPreHook() {
    return preHook;
  }

  public void setPreHook(PreHookDTO preHook) {
    this.preHook = preHook;
  }

  public PostHookDTO getPostHook() {
    return postHook;
  }

  public void setPostHook(PostHookDTO postHook) {
    this.postHook = postHook;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof AbstractLevelDTO))
      return false;
    AbstractLevelDTO other = (AbstractLevelDTO) obj;
    return Objects.equals(id, other.getId());
  }

  @Override
  public String toString() {
    return "AbstractLevelDTO [id=" + id + ", title=" + title + ", maxScore=" + maxScore + ", nextLevel=" + nextLevel
        + ", preHook=" + preHook + ", postHook=" + postHook + "]";
  }

}
