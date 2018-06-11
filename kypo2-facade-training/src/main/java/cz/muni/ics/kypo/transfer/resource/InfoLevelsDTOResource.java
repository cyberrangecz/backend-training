package cz.muni.ics.kypo.transfer.resource;

import java.util.List;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import com.fasterxml.jackson.annotation.JsonProperty;

import cz.muni.ics.kypo.transfer.ResultInfoDTO;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
@ApiObject(name = "Info Level Resource", description = "REST API resource with levels.")
public class InfoLevelsDTOResource<E> {

  @ApiObjectField(description = "Result info about returned levels.")
  @JsonProperty(required = true)
  private ResultInfoDTO page;
  @ApiObjectField(description = "List of info levels.")
  @JsonProperty(required = true)
  private List<E> infoLevels;

  public InfoLevelsDTOResource() {}

  public InfoLevelsDTOResource(List<E> infoLevels) {
    super();
    this.infoLevels = infoLevels;
  }

  public InfoLevelsDTOResource(ResultInfoDTO page, List<E> infoLevels) {
    super();
    this.page = page;
    this.infoLevels = infoLevels;
  }

  public List<E> getInfoLevels() {
    return infoLevels;
  }

  public void setInfoLevels(List<E> infoLevels) {
    this.infoLevels = infoLevels;
  }

  public ResultInfoDTO getPage() {
    return page;
  }

  public void setPage(ResultInfoDTO page) {
    this.page = page;
  }

  @Override
  public String toString() {
    return "InfoLevelsDTOResource [page=" + page + ", infoLevels=" + infoLevels + "]";
  }

}
