package cz.muni.csirt.kypo.events.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.events.game.common.GameDetails;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

/**
 * @author Pavel Šeda
 *
 */
@ApiObject(name = "Solution Closed", description = "Type of event from game scenario (when solution is closed).")
@JsonPropertyOrder({"type", "game_details", "timestamp"})
@JsonRootName(value = "event")
public class SolutionClosed extends AbstractAuditPOJO {

	@ApiObjectField(description = "Details of the game.")
	@JsonProperty(value = "game_details", required = true)
	private GameDetails gameDetails;

	public SolutionClosed(GameDetails gameDetails) {
		super();
		this.gameDetails = gameDetails;
	}

	public GameDetails getGameDetails() {
		return gameDetails;
	}

	public void setGameDetails(GameDetails gameDetails) {
		this.gameDetails = gameDetails;
	}

	@Override
	public String toString() {
		return "SolutionClosed [gameDetails=" + gameDetails + "]";
	}

}
