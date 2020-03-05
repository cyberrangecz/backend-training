package cz.muni.csirt.kypo.events.trainings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This classes uses Builder pattern based on the following blog:
 *
 * @see <a href="https://blog.jayway.com/2012/02/07/builder-pattern-with-a-twist/">https://blog.jayway.com/2012/02/07/builder-pattern-with-a-twist/</a>
 * <p>
 * Without that builder it is easy to mesh class parameters, e.g. trainingDefinitionId with trainingInstanceId.
 */
@ApiModel(value = "Hint Taken", description = "Type of event from trainings.")
@JsonRootName(value = "event")
public class HintTaken extends AbstractAuditPOJO {

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
    @ApiModelProperty(value = "Id of hint that is being taken", required = true)
    @JsonProperty(value = "hint_id", required = true)
    private long hintId;
    @ApiModelProperty(value = "Hint penalty points.", required = true)
    @JsonProperty(value = "hint_penalty_points", required = true)
    private int hintPenaltyPoints;
    @ApiModelProperty(value = "Hint title.", required = true)
    @JsonProperty(value = "hint_title", required = true)
    private String hintTitle;
    @ApiModelProperty(value = "Id of player in the training run")
    @JsonProperty(value = "user_ref_id")
    private long userRefId;
    @ApiModelProperty(value = "Issuer of player in the training run")
    @JsonProperty(value = "iss")
    private String iss;


    private HintTaken(HintTakenBuilder builder) {
        this.sandboxId = builder.sandboxId;
        this.poolId = builder.poolId;
        this.trainingDefinitionId = builder.trainingDefinitionId;
        this.trainingInstanceId = builder.trainingInstanceId;
        this.trainingRunId = builder.trainingRunId;
        this.gameTime = builder.gameTime;
        this.totalScore = builder.totalScore;
        this.actualScoreInLevel = builder.actualScoreInLevel;
        this.level = builder.level;
        this.hintId = builder.hintId;
        this.hintPenaltyPoints = builder.hintPenaltyPoints;
        this.hintTitle = builder.hintTitle;
        this.userRefId = builder.userRefId;
        this.iss = builder.iss;
    }

    public static class HintTakenBuilder {
        private long sandboxId;
        private long poolId;
        private long trainingDefinitionId;
        private long trainingInstanceId;
        private long trainingRunId;
        private long gameTime;
        private int totalScore;
        private int actualScoreInLevel;
        private long level;
        private long hintId;
        private int hintPenaltyPoints;
        private String hintTitle;
        private long userRefId;
        private String iss;

        public HintTakenBuilder sandboxId(long sandboxId) {
            this.sandboxId = sandboxId;
            return this;
        }

        public HintTakenBuilder poolId(long poolId) {
            this.poolId = poolId;
            return this;
        }

        public HintTakenBuilder trainingDefinitionId(long trainingDefinitionId) {
            this.trainingDefinitionId = trainingDefinitionId;
            return this;
        }

        public HintTakenBuilder trainingInstanceId(long trainingInstanceId) {
            this.trainingInstanceId = trainingInstanceId;
            return this;
        }

        public HintTakenBuilder trainingRunId(long trainingRunId) {
            this.trainingRunId = trainingRunId;
            return this;
        }

        public HintTakenBuilder gameTime(long gameTime) {
            this.gameTime = gameTime;
            return this;
        }

        public HintTakenBuilder totalScore(int totalScore) {
            this.totalScore = totalScore;
            return this;
        }

        public HintTakenBuilder actualScoreInLevel(int actualScoreInLevel) {
            this.actualScoreInLevel = actualScoreInLevel;
            return this;
        }

        public HintTakenBuilder level(long level) {
            this.level = level;
            return this;
        }

        public HintTakenBuilder hintId(long hintId) {
            this.hintId = hintId;
            return this;
        }

        public HintTakenBuilder hintPenaltyPoints(int penaltyPoints) {
            this.hintPenaltyPoints = penaltyPoints;
            return this;
        }

        public HintTakenBuilder hintTitle(String hintTitle) {
            this.hintTitle = hintTitle;
            return this;
        }

        public HintTakenBuilder userRefId(long userRefId) {
            this.userRefId = userRefId;
            return this;
        }

        public HintTakenBuilder iss(String iss) {
            this.iss = iss;
            return this;
        }

        public HintTaken build() {
            return new HintTaken(this);
        }

    }

    @Override
    public String toString() {
        return "HintTaken{" +
                "sandboxId=" + sandboxId +
                ", trainingDefinitionId=" + trainingDefinitionId +
                ", trainingInstanceId=" + trainingInstanceId +
                ", trainingRunId=" + trainingRunId +
                ", gameTime=" + gameTime +
                ", totalScore=" + totalScore +
                ", actualScoreInLevel=" + actualScoreInLevel +
                ", level=" + level +
                ", hintId=" + hintId +
                ", hintPenaltyPoints=" + hintPenaltyPoints +
                ", hintTitle='" + hintTitle + '\'' +
                ", userRefId=" + userRefId +
                ", iss='" + iss + '\'' +
                '}';
    }
}
