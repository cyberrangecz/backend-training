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
@ApiModel(value = "Solution Displayed", description = "Type of event from trainings.")
@JsonRootName(value = "event")
public class SolutionDisplayed extends AbstractAuditPOJO {

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
    @ApiModelProperty(value = "Total score of the player in the level.", required = true)
    @JsonProperty(value = "total_score", required = true)
    private int totalScore;
    @ApiModelProperty(value = "Full name of a player in the training run with titles.", required = true)
    @JsonProperty(value = "full_name", required = true)
    private String fullName;
    @ApiModelProperty(value = "Full name of a player in the training run without titles.", required = true)
    @JsonProperty(value = "full_name_without_titles", required = true)
    private String fullNameWithoutTitles;
    @ApiModelProperty(value = "Actual score of the player in the level.", required = true)
    @JsonProperty(value = "actual_score_in_level", required = true)
    private int actualScoreInLevel;
    @ApiModelProperty(value = "Training run level.", required = true)
    @JsonProperty(value = "level", required = true)
    private long level;
    @ApiModelProperty(value = "Solution displayed penalty points.", required = true)
    @JsonProperty(value = "penalty_points", required = true)
    private int penaltyPoints;

    private SolutionDisplayed(SolutionDisplayedBuilder builder) {
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
        this.penaltyPoints = builder.penaltyPoints;
    }

    public static class SolutionDisplayedBuilder {
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
        private int penaltyPoints;

        public SolutionDisplayedBuilder sandboxId(long sandboxId) {
            this.sandboxId = sandboxId;
            return this;
        }

        public SolutionDisplayedBuilder trainingDefinitionId(long trainingDefinitionId) {
            this.trainingDefinitionId = trainingDefinitionId;
            return this;
        }

        public SolutionDisplayedBuilder trainingInstanceId(long trainingInstanceId) {
            this.trainingInstanceId = trainingInstanceId;
            return this;
        }

        public SolutionDisplayedBuilder trainingRunId(long trainingRunId) {
            this.trainingRunId = trainingRunId;
            return this;
        }

        public SolutionDisplayedBuilder gameTime(long gameTime) {
            this.gameTime = gameTime;
            return this;
        }

        public SolutionDisplayedBuilder playerLogin(String playerLogin) {
            this.playerLogin = playerLogin;
            return this;
        }

        public SolutionDisplayedBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public SolutionDisplayedBuilder fullNameWithoutTitles(String fullNameWithoutTitles) {
            this.fullNameWithoutTitles = fullNameWithoutTitles;
            return this;
        }

        public SolutionDisplayedBuilder totalScore(int totalScore) {
            this.totalScore = totalScore;
            return this;
        }

        public SolutionDisplayedBuilder actualScoreInLevel(int actualScoreInLevel) {
            this.actualScoreInLevel = actualScoreInLevel;
            return this;
        }

        public SolutionDisplayedBuilder level(long level) {
            this.level = level;
            return this;
        }

        public SolutionDisplayedBuilder penaltyPoints(int penaltyPoints) {
            this.penaltyPoints = penaltyPoints;
            return this;
        }

        public SolutionDisplayed build() {
            return new SolutionDisplayed(this);
        }

    }

    @Override
    public String toString() {
        return "SolutionDisplayed{" +
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
                ", penaltyPoints=" + penaltyPoints +
                '}';
    }
}


