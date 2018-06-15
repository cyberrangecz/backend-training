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
@ApiObject(name = "Game Level Resource", description = "REST API resource with game levels.")
public class GameLevelsDTOResource<E> {

  @ApiObjectField(description = "Result info about returned game levels.")
  @JsonProperty(required = true)
  private ResultInfoDTO page;
  @ApiObjectField(description = "List of game levels.")
  @JsonProperty(required = true)
  private List<E> gameLevels;

  public GameLevelsDTOResource() {}

  public ResultInfoDTO getPage() {
    return page;
  }

  public void setPage(ResultInfoDTO page) {
    this.page = page;
  }

  public List<E> getGameLevels() {
    return gameLevels;
  }

  public void setGameLevels(List<E> gameLevels) {
    this.gameLevels = gameLevels;
  }

  @Override
  public String toString() {
    return "GameLevelsDTOResource [page=" + page + ", gameLevels=" + gameLevels + "]";
  }

}
