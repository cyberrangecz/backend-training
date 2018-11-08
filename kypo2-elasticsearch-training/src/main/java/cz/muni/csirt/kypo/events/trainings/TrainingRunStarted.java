package cz.muni.csirt.kypo.events.trainings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

/**
 * @author Pavel Šeda
 */
@ApiObject(name = "Training Run Started", description = "Type of event from trainings (when training run is started).")
@JsonRootName(value = "event")
public class TrainingRunStarted extends AbstractAuditPOJO {

    @ApiObjectField(description = "Training instance ID.")
    @JsonProperty(value = "training_instance_id", required = true)
    private long trainingInstanceId;
    @ApiObjectField(description = "Training run ID.")
    @JsonProperty(value = "training_run_id", required = true)
    private long trainingRunId;
    @ApiObjectField(description = "ID of a player in the training run.")
    @JsonProperty(value = "player_login", required = true)
    private String playerLogin;
    @ApiObjectField(description = "Training run level.")
    @JsonProperty(value = "level", required = true)
    private long level;

    public TrainingRunStarted(long trainingInstanceId, long trainingRunId, String playerLogin, long level) {
        super();
        this.trainingInstanceId = trainingInstanceId;
        this.trainingRunId = trainingRunId;
        this.playerLogin = playerLogin;
        this.level = level;
    }

    public long getTrainingInstanceId() {
        return trainingInstanceId;
    }

    public void setTrainingInstanceId(long trainingInstanceId) {
        this.trainingInstanceId = trainingInstanceId;
    }

    public long getTrainingRunId() {
        return trainingRunId;
    }

    public void setTrainingRunId(long trainingRunId) {
        this.trainingRunId = trainingRunId;
    }

    public String getPlayerLogin() {
        return playerLogin;
    }

    public void setPlayerLogin(String playerLogin) {
        this.playerLogin = playerLogin;
    }

    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = level;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GameStarted [trainingInstanceId=");
        builder.append(trainingInstanceId);
        builder.append(", trainingRunId=");
        builder.append(trainingRunId);
        builder.append(", playerLogin=");
        builder.append(playerLogin);
        builder.append(", level=");
        builder.append(level);
        builder.append("]");
        return builder.toString();
    }

}
