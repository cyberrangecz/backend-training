package cz.muni.csirt.kypo.elasticsearch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Seda
 */
@ApiModel(value = "Hint Taken", description = "Type of event from trainings.")
@JsonRootName(value = "event")
public class HintTakenDto extends AbstractAuditPojoDto {

    @ApiModelProperty(value = "Sandbox ID.", required = true)
    @JsonProperty(value = "sandbox_id", required = true)
    private long sandboxId;
    @ApiModelProperty(value = "Training definition ID.", required = true)
    @JsonProperty(value = "training_definition_id", required = true)
    private long trainingDefinitionId;
    @ApiModelProperty(value = "Training instance ID.", required = true)
    @JsonProperty(value = "training_instance_id", required = true)
    private long trainingInstanceId;
    @ApiModelProperty(value = "Training run ID.", required = true)
    @JsonProperty(value = "training_run_id", required = true)
    private long trainingRunId;
    @ApiModelProperty(value = "ID of a player in the training run.", required = true)
    @JsonProperty(value = "player_login", required = true)
    private String playerLogin;
    @ApiModelProperty(value = "Total score of the player in the level.", required = true)
    @JsonProperty(value = "total_score", required = true)
    private int totalScore;
    @ApiModelProperty(value = "Actual score of the player in the level.", required = true)
    @JsonProperty(value = "actual_score_in_level", required = true)
    private int actualScoreInLevel;
    @ApiModelProperty(value = "Training run level.", required = true)
    @JsonProperty(value = "level", required = true)
    private long level;
    @ApiModelProperty(value = "hint_id", required = true)
    @JsonProperty(value = "level", required = true)
    private long hintId;
    @ApiModelProperty(value = "Hint penalty points.", required = true)
    @JsonProperty(value = "hint_penalty_points", required = true)
    private int hintPenaltyPoints;
    @ApiModelProperty(value = "Hint title.", required = true)
    @JsonProperty(value = "hint_title", required = true)
    private String hintTitle;

    public HintTakenDto() {
    }

    public HintTakenDto(long sandboxId, long trainingDefinitionId, long trainingInstanceId, long trainingRunId, String playerLogin, int totalScore, int actualScoreInLevel, long level, long hintId, int hintPenaltyPoints, String hintTitle) {
        this.sandboxId = sandboxId;
        this.trainingDefinitionId = trainingDefinitionId;
        this.trainingInstanceId = trainingInstanceId;
        this.trainingRunId = trainingRunId;
        this.playerLogin = playerLogin;
        this.totalScore = totalScore;
        this.actualScoreInLevel = actualScoreInLevel;
        this.level = level;
        this.hintId = hintId;
        this.hintPenaltyPoints = hintPenaltyPoints;
        this.hintTitle = hintTitle;
    }

    public HintTakenDto(long timestamp, String type, long gameTime, long sandboxId, long trainingDefinitionId, long trainingInstanceId, long trainingRunId, String playerLogin, int totalScore, int actualScoreInLevel, long level, long hintId, int hintPenaltyPoints, String hintTitle) {
        super(timestamp, type, gameTime);
        this.sandboxId = sandboxId;
        this.trainingDefinitionId = trainingDefinitionId;
        this.trainingInstanceId = trainingInstanceId;
        this.trainingRunId = trainingRunId;
        this.playerLogin = playerLogin;
        this.totalScore = totalScore;
        this.actualScoreInLevel = actualScoreInLevel;
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

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getActualScoreInLevel() {
        return actualScoreInLevel;
    }

    public void setActualScoreInLevel(int actualScoreInLevel) {
        this.actualScoreInLevel = actualScoreInLevel;
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
}
