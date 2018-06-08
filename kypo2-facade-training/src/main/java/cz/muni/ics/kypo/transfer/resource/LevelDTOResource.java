package cz.muni.ics.kypo.transfer.resource;

import java.util.List;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
@ApiObject(name = "Level Resource", description = "REST API resource with levels.")
public class LevelDTOResource<E> {

  @ApiObjectField(description = "List of levels.")
  private List<E> levels;

  public LevelDTOResource() {}

  public LevelDTOResource(List<E> levels) {
    super();
    this.levels = levels;
  }

  public List<E> getLevels() {
    return levels;
  }

  public void setLevels(List<E> levels) {
    this.levels = levels;
  }

  @Override
  public String toString() {
    return "LevelDTOResource [levels=" + levels + "]";
  }

}
