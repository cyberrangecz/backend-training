package cz.muni.ics.kypo.api.dto;

import cz.muni.ics.kypo.model.AbstractLevel;
import io.swagger.annotations.ApiModel;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@ApiModel(value = "PreHookDTO", description = ".")
public class PreHookDTO {

  private Long id;
  private AbstractLevel abstractLevel;

  public PreHookDTO() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public AbstractLevel getAbstractLevel() {
    return abstractLevel;
  }

  public void setAbstractLevel(AbstractLevel abstractLevel) {
    this.abstractLevel = abstractLevel;
  }

  @Override
  public String toString() {
    return "PreHookDTO [id=" + id + ", abstractLevel=" + abstractLevel + "]";
  }

}
