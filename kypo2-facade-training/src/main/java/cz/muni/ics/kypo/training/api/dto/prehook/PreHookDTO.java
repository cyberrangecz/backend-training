package cz.muni.ics.kypo.training.api.dto.prehook;

import io.swagger.annotations.ApiModel;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@ApiModel(value = "PreHookDTO", description = ".")
public class PreHookDTO {

  private Long id;

	public Long getId() {
		return id;
	}

  public void setId(Long id) {
    this.id = id;
  }

  @Override public String toString() {
    return "PreHookDTO{" + "id=" + id + '}';
  }
}
