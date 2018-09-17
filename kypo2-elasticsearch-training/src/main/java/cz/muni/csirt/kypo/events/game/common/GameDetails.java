package cz.muni.csirt.kypo.events.game.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

/**
 * 
 * @author Pavel Šeda
 *
 */
@ApiObject(name = "Game Details", description = "Details of particular Game.")
@JsonPropertyOrder({"game_instance_id", "level", "logical_time", "player_id"})
public class GameDetails {

  @ApiObjectField(description = "Instance ID of game.")
  @JsonProperty(value = "game_instance_id", required = true)
  private int gameInstanceId;
  @ApiObjectField(description = "Level of game.")
  @JsonProperty(value = "level", required = true)
  private int level;
  @ApiObjectField(description = "Game logical time.")
  @JsonProperty(value = "logical_time", required = true)
  private int logicalTime;
  @ApiObjectField(description = "ID of a player in the game.")
  @JsonProperty(value = "player_id", required = true)
  private int playerId;

  public GameDetails() {}

  public GameDetails(int gameInstanceId, int level, int logicalTime, int playerId) {
    super();
    this.gameInstanceId = gameInstanceId;
    this.level = level;
    this.logicalTime = logicalTime;
    this.playerId = playerId;
  }

  public int getGameInstanceId() {
    return gameInstanceId;
  }

  public void setGameInstanceId(int gameInstanceId) {
    this.gameInstanceId = gameInstanceId;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public int getLogicalTime() {
    return logicalTime;
  }

  public void setLogicalTime(int logicalTime) {
    this.logicalTime = logicalTime;
  }

  public int getPlayerId() {
    return playerId;
  }

  public void setPlayerId(int playerId) {
    this.playerId = playerId;
  }

  @Override
  public String toString() {
    return "GameDetails [gameInstanceId=" + gameInstanceId + ", level=" + level + ", logicalTime=" + logicalTime + ", playerId=" + playerId + "]";
  }

}
