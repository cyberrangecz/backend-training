package cz.muni.csirt.kypo.events.trainings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

/**
 * @author Pavel Šeda
 */
@ApiObject(name = "Hint Taken", description = "Type of event from trainings.")
@JsonRootName(value = "event")
public class HintTaken extends AbstractAuditPOJO {

    @ApiObjectField(description = "Sandbox ID.")
    @JsonProperty(value = "sandbox_id", required = true)
    private long sandboxId;
    @ApiObjectField(description = "Training definition ID.")
    @JsonProperty(value = "training_definition_id", required = true)
    private long trainingDefinitionId;
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
    @ApiObjectField(description = "Hint ID.")
    @JsonProperty(value = "hint_id", required = true)
    private long hintId;
    @ApiObjectField(description = "Hint penalty points.")
    @JsonProperty(value = "hint_penalty_points", required = true)
    private int hintPenaltyPoints;
    @ApiObjectField(description = "Hint title.")
    @JsonProperty(value = "hint_title", required = true)
    private String hintTitle;

    public HintTaken(long sandboxId, long trainingDefinitionId, long trainingInstanceId, long trainingRunId, String playerLogin, long level, long hintId, int hintPenaltyPoints, String hintTitle) {
        this.sandboxId = sandboxId;
        this.trainingDefinitionId = trainingDefinitionId;
        this.trainingInstanceId = trainingInstanceId;
        this.trainingRunId = trainingRunId;
        this.playerLogin = playerLogin;
        this.level = level;
        this.hintId = hintId;
        this.hintPenaltyPoints = hintPenaltyPoints;
        this.hintTitle = hintTitle;
    }

    public long getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(long sandboxId) {
        this.sandboxId = sandboxId;
    }

    public long getTrainingDefinitionId() {
        return trainingDefinitionId;
    }

    public void setTrainingDefinitionId(long trainingDefinitionId) {
        this.trainingDefinitionId = trainingDefinitionId;
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

    public long getHintId() {
        return hintId;
    }

    public void setHintId(long hintId) {
        this.hintId = hintId;
    }

    public int getHintPenaltyPoints() {
        return hintPenaltyPoints;
    }

    public void setHintPenaltyPoints(int hintPenaltyPoints) {
        this.hintPenaltyPoints = hintPenaltyPoints;
    }

    public String getHintTitle() {
        return hintTitle;
    }

    public void setHintTitle(String hintTitle) {
        this.hintTitle = hintTitle;
    }

    @Override
    public String toString() {
        return "HintTaken{" +
                "sandboxId=" + sandboxId +
                ", trainingDefinitionId=" + trainingDefinitionId +
                ", trainingInstanceId=" + trainingInstanceId +
                ", trainingRunId=" + trainingRunId +
                ", playerLogin='" + playerLogin + '\'' +
                ", level=" + level +
                ", hintId=" + hintId +
                ", hintPenaltyPoints=" + hintPenaltyPoints +
                ", hintTitle='" + hintTitle + '\'' +
                '}';
    }
}
