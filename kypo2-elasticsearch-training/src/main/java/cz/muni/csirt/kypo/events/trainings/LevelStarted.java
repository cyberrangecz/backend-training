package cz.muni.csirt.kypo.events.trainings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.events.trainings.enums.LevelType;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

/**
 * @author Pavel Šeda
 */
@ApiObject(name = "Level Started", description = "Type of event from trainings (when level is started).")
@JsonRootName(value = "event")
public class LevelStarted extends AbstractAuditPOJO {

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
    @ApiObjectField(description = "Level Type.")
    @JsonProperty(value = "level_type", required = true)
    private LevelType levelType;
    @ApiObjectField(description = "Max Score.")
    @JsonProperty(value = "max_score", required = true)
    private int maxScore;
    @JsonProperty(value = "level_title", required = true)
    private String levelTitle;

    public LevelStarted(long sandboxId, long trainingDefinitionId, long trainingInstanceId, long trainingRunId, String playerLogin, long level, LevelType levelType, int maxScore, String levelTitle) {
        this.sandboxId = sandboxId;
        this.trainingDefinitionId = trainingDefinitionId;
        this.trainingInstanceId = trainingInstanceId;
        this.trainingRunId = trainingRunId;
        this.playerLogin = playerLogin;
        this.level = level;
        this.levelType = levelType;
        this.maxScore = maxScore;
        this.levelTitle = levelTitle;
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

    public LevelType getLevelType() {
        return levelType;
    }

    public void setLevelType(LevelType levelType) {
        this.levelType = levelType;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public String getLevelTitle() {
        return levelTitle;
    }

    public void setLevelTitle(String levelTitle) {
        this.levelTitle = levelTitle;
    }

    @Override
    public String toString() {
        return "LevelStarted{" +
                "sandboxId=" + sandboxId +
                ", trainingDefinitionId=" + trainingDefinitionId +
                ", trainingInstanceId=" + trainingInstanceId +
                ", trainingRunId=" + trainingRunId +
                ", playerLogin='" + playerLogin + '\'' +
                ", level=" + level +
                ", levelType=" + levelType +
                ", maxScore=" + maxScore +
                ", levelTitle='" + levelTitle + '\'' +
                '}';
    }
}
