package cz.muni.csirt.kypo.events.trainings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Šeda
 */
@ApiModel(value = "Training Run Started", description = "Type of event from trainings (when training run is started).")
@JsonRootName(value = "event")
public class TrainingRunStarted extends AbstractAuditPOJO {

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
    @ApiModelProperty(value = "The time in particular training run (in particular game).", required = true)
    @JsonProperty(value = "game_time", required = true)
    private long gameTime;
    @ApiModelProperty(value = "ID of a player in the training run.", required = true)
    @JsonProperty(value = "player_login", required = true)
    private String playerLogin;
    @ApiModelProperty(value = "Full name of a player in the training run with titles.", required = true)
    @JsonProperty(value = "full_name", required = true)
    private String fullName;
    @ApiModelProperty(value = "Full name of a player in the training run without titles.", required = true)
    @JsonProperty(value = "full_name_without_titles", required = true)
    private String fullNameWithoutTitles;
    @ApiModelProperty(value = "Total score of the player in the level.", required = true)
    @JsonProperty(value = "total_score", required = true)
    private int totalScore;
    @ApiModelProperty(value = "Actual score of the player in the level.", required = true)
    @JsonProperty(value = "actual_score_in_level", required = true)
    private int actualScoreInLevel;
    @ApiModelProperty(value = "Training run level.", required = true)
    @JsonProperty(value = "level", required = true)
    private long level;

    private TrainingRunStarted(TrainingRunStartedBuilder builder) {
        this.sandboxId = builder.sandboxId;
        this.trainingDefinitionId = builder.trainingDefinitionId;
        this.trainingInstanceId = builder.trainingInstanceId;
        this.trainingRunId = builder.trainingRunId;
        this.gameTime = builder.gameTime;
        this.playerLogin = builder.playerLogin;
        this.fullName = builder.fullName;
        this.fullNameWithoutTitles = builder.fullNameWithoutTitles;
        this.totalScore = builder.totalScore;
        this.actualScoreInLevel = builder.actualScoreInLevel;
        this.level = builder.level;
    }

    public static class TrainingRunStartedBuilder {
        private long sandboxId;
        private long trainingDefinitionId;
        private long trainingInstanceId;
        private long trainingRunId;
        private long gameTime;
        private String playerLogin;
        private String fullName;
        private String fullNameWithoutTitles;
        private int totalScore;
        private int actualScoreInLevel;
        private long level;

        public TrainingRunStartedBuilder sandboxId(long sandboxId) {
            this.sandboxId = sandboxId;
            return this;
        }

        public TrainingRunStartedBuilder trainingDefinitionId(long trainingDefinitionId) {
            this.trainingDefinitionId = trainingDefinitionId;
            return this;
        }

        public TrainingRunStartedBuilder trainingInstanceId(long trainingInstanceId) {
            this.trainingInstanceId = trainingInstanceId;
            return this;
        }

        public TrainingRunStartedBuilder trainingRunId(long trainingRunId) {
            this.trainingRunId = trainingRunId;
            return this;
        }

        public TrainingRunStartedBuilder gameTime(long gameTime) {
            this.gameTime = gameTime;
            return this;
        }

        public TrainingRunStartedBuilder playerLogin(String playerLogin) {
            this.playerLogin = playerLogin;
            return this;
        }

        public TrainingRunStartedBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public TrainingRunStartedBuilder fullNameWithoutTitles(String fullNameWithoutTitles) {
            this.fullNameWithoutTitles = fullNameWithoutTitles;
            return this;
        }

        public TrainingRunStartedBuilder totalScore(int totalScore) {
            this.totalScore = totalScore;
            return this;
        }

        public TrainingRunStartedBuilder actualScoreInLevel(int actualScoreInLevel) {
            this.actualScoreInLevel = actualScoreInLevel;
            return this;
        }

        public TrainingRunStartedBuilder level(long level) {
            this.level = level;
            return this;
        }

        public TrainingRunStarted build() {
            return new TrainingRunStarted(this);
        }
    }

    @Override
    public String toString() {
        return "TrainingRunStarted{" +
                "sandboxId=" + sandboxId +
                ", trainingDefinitionId=" + trainingDefinitionId +
                ", trainingInstanceId=" + trainingInstanceId +
                ", trainingRunId=" + trainingRunId +
                ", gameTime=" + gameTime +
                ", playerLogin='" + playerLogin + '\'' +
                ", fullName='" + fullName + '\'' +
                ", fullNameWithoutTitles='" + fullNameWithoutTitles + '\'' +
                ", totalScore=" + totalScore +
                ", actualScoreInLevel=" + actualScoreInLevel +
                ", level=" + level +
                '}';
    }
}
