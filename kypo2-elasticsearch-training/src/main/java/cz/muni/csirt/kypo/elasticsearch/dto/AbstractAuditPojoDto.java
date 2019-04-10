package cz.muni.csirt.kypo.elasticsearch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Seda
 */
@ApiModel(value = "Parent class for all audit POJO classes",
        description = "This class have to be extended when some event should be saved to Elasticsearch."
                + " It provides 2 member variables 'timestamp' and 'type': 'timestamp' is generated based on current time 'type'"
                + " is generated based on your pojoClass (name of package + class name).")
@JsonPropertyOrder({"type", "timestamp"})
public class AbstractAuditPojoDto {

    @ApiModelProperty(value = "The time at which the event occurred.", required = true)
    @JsonProperty(value = "timestamp", required = true)
    private long timestamp;
    @ApiModelProperty(value = "Type of event.", required = true)
    @JsonProperty(value = "type", required = true)
    private String type;
    @ApiModelProperty(value = "Game time.", required = true)
    @JsonProperty(value = "game_time", required = true)
    private long gameTime;

    public AbstractAuditPojoDto() {
    }

    public AbstractAuditPojoDto(long timestamp, String type, long gameTime) {
        this.timestamp = timestamp;
        this.type = type;
        this.gameTime = gameTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getGameTime() {
        return gameTime;
    }

    public void setGameTime(long gameTime) {
        this.gameTime = gameTime;
    }

    @Override
    public String toString() {
        return "AbstractAuditPojoDto{" +
                "timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", gameTime=" + gameTime +
                '}';
    }
}
