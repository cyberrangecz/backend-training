package cz.muni.csirt.kypo.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This class have to be extended when some event should be saved to Elasticsearch. It provides 2
 * member variables 'timestamp' and 'type': 'timestamp' is generated based on current time 'type' is
 * generated based on your pojoClass (name of package + class name)
 */
@ApiModel(value = "Parent class for all audit POJO classes",
        description = "This class have to be extended when some event should be saved to Elasticsearch."
                + " It provides 2 member variables 'timestamp' and 'type': 'timestamp' is generated based on current time 'type'"
                + " is generated based on your pojoClass (name of package + class name).")
@JsonPropertyOrder({"type", "timestamp"})
public abstract class AbstractAuditPOJO {

    @ApiModelProperty(value = "The time at which the event occurred.", required = true)
    @JsonProperty(value = "timestamp", required = true)
    private long timestamp;
    @ApiModelProperty(value = "Type of event.", required = true)
    @JsonProperty(value = "type", required = true)
    private String type;

    /**
     * Instantiates a new Abstract audit pojo.
     */
    public AbstractAuditPOJO() {
    }

    /**
     * Instantiates a new Abstract audit pojo.
     *
     * @param timestamp the timestamp
     * @param type      the type
     */
    public AbstractAuditPOJO(long timestamp, String type) {
        super();
        this.timestamp = timestamp;
        this.type = type;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets timestamp.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "AbstractAuditPOJO [timestamp=" + timestamp + ", type=" + type + "]";
    }

}
