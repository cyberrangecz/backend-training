package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModel;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@ApiModel(value = "PreHookDTO", description = ".")
public class PreHookDTO {

  private Long id;
  private AbstractLevelDTO abstractLevel;

  public PreHookDTO() {}

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
    return "PreHookDTO [id=" + id + ", abstractLevel=" + abstractLevel + "]";
  }

}
