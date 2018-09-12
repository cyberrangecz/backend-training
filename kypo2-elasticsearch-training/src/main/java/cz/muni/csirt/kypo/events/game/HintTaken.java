package cz.muni.csirt.kypo.events.game;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.events.game.common.GameDetails;

/**
 * @author Pavel Šeda
 *
 */
@ApiObject(name = "Hint Taken", description = "Type of event from game scenario (when hint is taken).")
@JsonPropertyOrder({"type", "game_details", "timestamp", "hint_id"})
@JsonRootName(value = "event")
public class HintTaken extends AbstractAuditPOJO {

	@ApiObjectField(description = "Details of the game.")
	@JsonProperty(value = "game_details", required = true)
	private GameDetails gameDetails;
	@ApiObjectField(description = "ID of used hint.")
	@JsonProperty(value = "hint_id", required = true)
	private int hintId;

	public HintTaken(GameDetails gameDetails, int hintId) {
		super();
		this.gameDetails = gameDetails;
		this.hintId = hintId;
	}

	public GameDetails getGameDetails() {
		return gameDetails;
	}

	public void setGameDetails(GameDetails gameDetails) {
		this.gameDetails = gameDetails;
	}

	public int getHintId() {
		return hintId;
	}

	public void setHintId(int hintId) {
		this.hintId = hintId;
	}

	@Override
	public String toString() {
		return "HintTaken [gameDetails=" + gameDetails + ", hintId=" + hintId + "]";
	}

}
