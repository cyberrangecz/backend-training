package cz.muni.csirt.kypo.events.game;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.events.game.common.GameDetails;

/**
 * 
 * @author Pavel Šeda
 *
 */
@ApiObject(name = "Wrong Flag Submitted", description = "Type of event from game scenario (when wrong flag is used).")
@JsonPropertyOrder({"type", "game_details", "timestamp", "value"})
@JsonRootName(value = "event")
public class WrongFlagSubmitted extends AbstractAuditPOJO {

  @ApiObjectField(description = "Details of the game.")
  @JsonProperty(value = "game_details", required = true)
  private GameDetails gameDetails;
  @ApiObjectField(description = "Value.") // TODO describe in more detail what is this value
                                          // attribute
  @JsonProperty(value = "value", required = true)
  private String value;

  public WrongFlagSubmitted(GameDetails GameDetailsWithStrings, String value) {
    super();
    this.gameDetails = GameDetailsWithStrings;
    this.value = value;
  }

  public GameDetails getGameDetails() {
    return gameDetails;
  }

  public void setGameDetails(GameDetails gameDetails) {
    this.gameDetails = gameDetails;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "WrongFlagSubmitted [gameDetails=" + gameDetails + ", value=" + value + "]";
  }

}
