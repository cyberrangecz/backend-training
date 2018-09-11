package cz.muni.csirt.kypo.events.game.common;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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
  private long gameInstanceId;
  @ApiObjectField(description = "Level of game.")
  @JsonProperty(value = "level", required = true)
  private long level;
  @ApiObjectField(description = "Game logical time.")
  @JsonProperty(value = "logical_time", required = true)
  private long logicalTime;
  @ApiObjectField(description = "ID of a player in the game.")
  @JsonProperty(value = "player_id", required = true)
  private long playerId;

  public GameDetails(long gameInstanceId, long level, long logicalTime, long playerId) {
    super();
    this.gameInstanceId = gameInstanceId;
    this.level = level;
    this.logicalTime = logicalTime;
    this.playerId = playerId;
  }

  public long getGameInstanceId() {
    return gameInstanceId;
  }

  public void setGameInstanceId(long gameInstanceId) {
    this.gameInstanceId = gameInstanceId;
  }

  public long getLevel() {
    return level;
  }

  public void setLevel(long level) {
    this.level = level;
  }

  public long getLogicalTime() {
    return logicalTime;
  }

  public void setLogicalTime(long logicalTime) {
    this.logicalTime = logicalTime;
  }

  public long getPlayerId() {
    return playerId;
  }

  public void setPlayerId(long playerId) {
    this.playerId = playerId;
  }

  @Override
  public String toString() {
    return "GameDetails [gameInstanceId=" + gameInstanceId + ", level=" + level + ", logicalTime=" + logicalTime + ", playerId=" + playerId + "]";
  }

}
