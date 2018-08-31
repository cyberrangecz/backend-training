package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModel;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@ApiModel(value = "PostHookDTO", description = ".")
public class PostHookDTO {

  private Long id;
  private AbstractLevelDTO abstractLevel;

  public PostHookDTO() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public AbstractLevelDTO getAbstractLevel() {
    return abstractLevel;
  }

  public void setAbstractLevel(AbstractLevelDTO abstractLevel) {
    this.abstractLevel = abstractLevel;
  }

  @Override
  public String toString() {
    return "PostHookDTO [id=" + id + ", abstractLevel=" + abstractLevel + "]";
  }

}
