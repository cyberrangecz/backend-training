package cz.muni.csirt.kypo.events.trainings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This classes uses Builder pattern based on the following blog:
 *
 * @author Pavel Šeda
 * @see <a href="https://blog.jayway.com/2012/02/07/builder-pattern-with-a-twist/">https://blog.jayway.com/2012/02/07/builder-pattern-with-a-twist/</a>
 * <p>
 * Without that builder it is easy to mesh class parameters, e.g. trainingDefinitionId with trainingInstanceId.
 */
@ApiModel(value = "Training Run Ended", description = "Type of event from trainings.")
@JsonRootName(value = "event")
public class TrainingRunEnded extends AbstractAuditPOJO {

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

    public static SandboxIdBuilder builder() {
        return new TrainingRunEndedBuilder();
    }

    public static class TrainingRunEndedBuilder implements SandboxIdBuilder, TrainingDefinitionIdBuilder, TrainingInstanceIdBuilder, TrainingRunIdBuilder, PlayerLoginBuilder, TotalScoreBuilder, ActualScoreInLevelBuilder, LevelBuilder {
        private long sandboxId;
        private long trainingDefinitionId;
        private long trainingInstanceId;
        private long trainingRunId;
        private String playerLogin;
        private int totalScore;
        private int actualScoreInLevel;
        private long level;

        @Override
        public TrainingDefinitionIdBuilder sandboxId(long sandboxId) {
            this.sandboxId = sandboxId;
            return this;
        }

        @Override
        public TrainingInstanceIdBuilder trainingDefinitionId(long trainingDefinitionId) {
            this.trainingDefinitionId = trainingDefinitionId;
            return this;
        }

        @Override
        public TrainingRunIdBuilder trainingInstanceId(long trainingInstanceId) {
            this.trainingInstanceId = trainingInstanceId;
            return this;
        }

        @Override
        public PlayerLoginBuilder trainingRunId(long trainingRunId) {
            this.trainingRunId = trainingRunId;
            return this;
        }

        @Override
        public TotalScoreBuilder playerLogin(String playerLogin) {
            this.playerLogin = playerLogin;
            return this;
        }

        @Override
        public ActualScoreInLevelBuilder totalScore(int totalScore) {
            this.totalScore = totalScore;
            return this;
        }

        @Override
        public LevelBuilder actualScoreInLevel(int actualScoreInLevel) {
            this.actualScoreInLevel = actualScoreInLevel;
            return this;
        }

        @Override
        public TrainingRunEndedBuilder level(long level) {
            this.level = level;
            return this;
        }

        public TrainingRunEnded build() {
            return new TrainingRunEnded(this);
        }
    }

    public interface SandboxIdBuilder {
        TrainingDefinitionIdBuilder sandboxId(long sandboxId);
    }

    public interface TrainingDefinitionIdBuilder {
        TrainingInstanceIdBuilder trainingDefinitionId(long trainingDefinitionId);
    }

    public interface TrainingInstanceIdBuilder {
        TrainingRunIdBuilder trainingInstanceId(long trainingInstanceId);
    }

    public interface TrainingRunIdBuilder {
        PlayerLoginBuilder trainingRunId(long trainingRunId);
    }

    public interface PlayerLoginBuilder {
        TotalScoreBuilder playerLogin(String playerLogin);
    }

    public interface TotalScoreBuilder {
        ActualScoreInLevelBuilder totalScore(int totalScore);
    }

    public interface ActualScoreInLevelBuilder {
        LevelBuilder actualScoreInLevel(int actualScoreInLevel);
    }

    public interface LevelBuilder {
        TrainingRunEndedBuilder level(long level);
    }

    private TrainingRunEnded(TrainingRunEndedBuilder builder) {
        this.sandboxId = builder.sandboxId;
        this.trainingDefinitionId = builder.trainingDefinitionId;
        this.trainingInstanceId = builder.trainingInstanceId;
        this.trainingRunId = builder.trainingRunId;
        this.playerLogin = builder.playerLogin;
        this.totalScore = builder.totalScore;
        this.actualScoreInLevel = builder.actualScoreInLevel;
        this.level = builder.level;
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

    @Override
    public String toString() {
        return "TrainingRunEndedDto{" +
                "sandboxId=" + sandboxId +
                ", trainingDefinitionId=" + trainingDefinitionId +
                ", trainingInstanceId=" + trainingInstanceId +
                ", trainingRunId=" + trainingRunId +
                ", playerLogin='" + playerLogin + '\'' +
                ", level=" + level +
                '}';
    }
}
