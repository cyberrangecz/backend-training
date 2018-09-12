package cz.muni.csirt.kypo.elasticsearch;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This class have to be extended when some event should be saved to Elasticsearch. It provides 2
 * member variables 'timestamp' and 'type': 'timestamp' is generated based on current time 'type' is
 * generated based on your pojoClass (name of package + class name)
 * 
 * @author Pavel Šeda
 *
 */
@ApiObject(name = "Parent class for all audit POJO classes",
	description = "This class have to be extended when some event should be saved to Elasticsearch."
			+ " It provides 2 member variables 'timestamp' and 'type': 'timestamp' is generated based on current time 'type'"
			+ " is generated based on your pojoClass (name of package + class name).")
@JsonPropertyOrder({"type", "timestamp"})
public abstract class AbstractAuditPOJO {

	@ApiObjectField(description = "The time at which the event occurred.")
	@JsonProperty(value = "timestamp", required = true)
	private long timestamp;
	@ApiObjectField(
		description = "Type of event, e.g. cz.muni.csirt.kypo.WrongFlagSubmitted (this type is derived from the name of the package and the class name).")
	@JsonProperty(value = "type", required = true)
	private String type;

	public AbstractAuditPOJO() {}

	public AbstractAuditPOJO(long timestamp, String type) {
		super();
		this.timestamp = timestamp;
		this.type = type;
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

	@Override
	public String toString() {
		return "AbstractAuditPOJO [timestamp=" + timestamp + ", type=" + type + "]";
	}

}
