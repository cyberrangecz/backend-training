package cz.muni.csirt.kypo.events.trainings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.events.trainings.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This classes uses Builder pattern based on the following blog:
 *
 * @see <a href="https://blog.jayway.com/2012/02/07/builder-pattern-with-a-twist/">https://blog.jayway.com/2012/02/07/builder-pattern-with-a-twist/</a>
 * <p>
 * Without that builder it is easy to mesh class parameters, e.g. trainingDefinitionId with trainingInstanceId.
 */
@ApiModel(value = "Level Started", description = "Type of event from trainings (when level is started).")
@JsonRootName(value = "event")
public class LevelStarted extends AbstractAuditPOJO {

    @ApiModelProperty(value = "Sandbox ID.", required = true)
    @JsonProperty(value = "sandbox_id", required = true)
    private long sandboxId;
    @ApiModelProperty(value = "Pool ID.", required = true)
    @JsonProperty(value = "pool_id", required = true)
    private long poolId;
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
    @ApiModelProperty(value = "Total score of the player in the level.", required = true)
    @JsonProperty(value = "total_score", required = true)
    private int totalScore;
    @ApiModelProperty(value = "Actual score of the player in the level.", required = true)
    @JsonProperty(value = "actual_score_in_level", required = true)
    private int actualScoreInLevel;
    @ApiModelProperty(value = "Training run level.", required = true)
    @JsonProperty(value = "level", required = true)
    private long level;
    @ApiModelProperty(value = "Level Type.", required = true)
    @JsonProperty(value = "level_type", required = true)
    private LevelType levelType;
    @ApiModelProperty(value = "Max Score.", required = true)
    @JsonProperty(value = "max_score", required = true)
    private int maxScore;
    @ApiModelProperty(value = "Level title.", required = true)
    @JsonProperty(value = "level_title", required = true)
    private String levelTitle;
    @ApiModelProperty(value = "Id of player in the training run")
    @JsonProperty(value = "user_ref_id")
    private long userRefId;
    @ApiModelProperty(value = "Issuer of player in the training run")
    @JsonProperty(value = "iss")
    private String iss;

    private LevelStarted(LevelStartedBuilder builder) {
        this.sandboxId = builder.sandboxId;
        this.poolId = builder.poolId;
        this.trainingDefinitionId = builder.trainingDefinitionId;
        this.trainingInstanceId = builder.trainingInstanceId;
        this.trainingRunId = builder.trainingRunId;
        this.gameTime = builder.gameTime;
        this.totalScore = builder.totalScore;
        this.actualScoreInLevel = builder.actualScoreInLevel;
        this.level = builder.level;
        this.levelType = builder.levelType;
        this.maxScore = builder.maxScore;
        this.levelTitle = builder.levelTitle;
        this.userRefId = builder.userRefId;
        this.iss = builder.iss;
    }

    public static class LevelStartedBuilder {
        private long sandboxId;
        private long poolId;
        private long trainingDefinitionId;
        private long trainingInstanceId;
        private long trainingRunId;
        private long gameTime;
        private int totalScore;
        private int actualScoreInLevel;
        private long level;
        private LevelType levelType;
        private int maxScore;
        private String levelTitle;
        private long userRefId;
        private String iss;

        public LevelStartedBuilder sandboxId(long sandboxId) {
            this.sandboxId = sandboxId;
            return this;
        }

        public LevelStartedBuilder poolId(long poolId) {
            this.poolId = poolId;
            return this;
        }

        public LevelStartedBuilder trainingDefinitionId(long trainingDefinitionId) {
            this.trainingDefinitionId = trainingDefinitionId;
            return this;
        }

        public LevelStartedBuilder trainingInstanceId(long trainingInstanceId) {
            this.trainingInstanceId = trainingInstanceId;
            return this;
        }

        public LevelStartedBuilder trainingRunId(long trainingRunId) {
            this.trainingRunId = trainingRunId;
            return this;
        }

        public LevelStartedBuilder gameTime(long gameTime) {
            this.gameTime = gameTime;
            return this;
        }

        public LevelStartedBuilder totalScore(int totalScore) {
            this.totalScore = totalScore;
            return this;
        }

        public LevelStartedBuilder actualScoreInLevel(int actualScoreInLevel) {
            this.actualScoreInLevel = actualScoreInLevel;
            return this;
        }

        public LevelStartedBuilder level(long level) {
            this.level = level;
            return this;
        }

        public LevelStartedBuilder levelType(LevelType levelType) {
            this.levelType = levelType;
            return this;
        }

        public LevelStartedBuilder maxScore(int maxScore) {
            this.maxScore = maxScore;
            return this;
        }

        public LevelStartedBuilder levelTitle(String levelTitle) {
            this.levelTitle = levelTitle;
            return this;
        }

        public LevelStartedBuilder userRefId(long userRefId) {
            this.userRefId = userRefId;
            return this;
        }

        public LevelStartedBuilder iss(String iss) {
            this.iss = iss;
            return this;
        }

        public LevelStarted build() {
            return new LevelStarted(this);
        }

    }

    @Override
    public String toString() {
        return "LevelStarted{" +
                "sandboxId=" + sandboxId +
                ", trainingDefinitionId=" + trainingDefinitionId +
                ", trainingInstanceId=" + trainingInstanceId +
                ", trainingRunId=" + trainingRunId +
                ", gameTime=" + gameTime +
                ", totalScore=" + totalScore +
                ", actualScoreInLevel=" + actualScoreInLevel +
                ", level=" + level +
                ", levelType=" + levelType +
                ", maxScore=" + maxScore +
                ", levelTitle='" + levelTitle + '\'' +
                ", userRefId=" + userRefId +
                ", iss='" + iss + '\'' +
                '}';
    }
}
