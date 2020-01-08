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
 * <p>
 * Without that builder it is easy to mesh class parameters, e.g. trainingDefinitionId with trainingInstanceId.
 */
@ApiModel(value = "Assessment Answers Started", description = "Type of event from trainings.")
@JsonRootName(value = "event")
public class AssessmentAnswers extends AbstractAuditPOJO {

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
    @ApiModelProperty(value = "Total score of the player in the level.", required = true)
    @JsonProperty(value = "total_score", required = true)
    private int totalScore;
    @ApiModelProperty(value = "Actual score of the player in the level.", required = true)
    @JsonProperty(value = "actual_score_in_level", required = true)
    private int actualScoreInLevel;
    @ApiModelProperty(value = "Training run level.", required = true)
    @JsonProperty(value = "level", required = true)
    private long level;
    @ApiModelProperty(value = "Only on assessment level type, data format is JSON.", required = false)
    @JsonProperty(value = "answers", required = false)
    private String answers;
    @ApiModelProperty(value = "Id of player in the training run")
    @JsonProperty(value = "user_ref_id")
    private long userRefId;
    @ApiModelProperty(value = "Issuer of player in the training run")
    @JsonProperty(value = "iss")
    private String iss;

    private AssessmentAnswers(AssessmentAnswersBuilder builder) {
        this.sandboxId = builder.sandboxId;
        this.trainingDefinitionId = builder.trainingDefinitionId;
        this.trainingInstanceId = builder.trainingInstanceId;
        this.trainingRunId = builder.trainingRunId;
        this.gameTime = builder.gameTime;
        this.totalScore = builder.totalScore;
        this.actualScoreInLevel = builder.actualScoreInLevel;
        this.level = builder.level;
        this.answers = builder.answers;
        this.userRefId = builder.userRefId;
        this.iss = builder.iss;
    }

    public static class AssessmentAnswersBuilder {
        private long sandboxId;
        private long trainingDefinitionId;
        private long trainingInstanceId;
        private long trainingRunId;
        private long gameTime;
        private int totalScore;
        private int actualScoreInLevel;
        private long level;
        private String answers;
        private long userRefId;
        private String iss;

        public AssessmentAnswersBuilder sandboxId(long sandboxId) {
            this.sandboxId = sandboxId;
            return this;
        }

        public AssessmentAnswersBuilder trainingDefinitionId(long trainingDefinitionId) {
            this.trainingDefinitionId = trainingDefinitionId;
            return this;
        }

        public AssessmentAnswersBuilder trainingInstanceId(long trainingInstanceId) {
            this.trainingInstanceId = trainingInstanceId;
            return this;
        }

        public AssessmentAnswersBuilder trainingRunId(long trainingRunId) {
            this.trainingRunId = trainingRunId;
            return this;
        }

        public AssessmentAnswersBuilder gameTime(long gameTime) {
            this.gameTime = gameTime;
            return this;
        }

        public AssessmentAnswersBuilder totalScore(int totalScore) {
            this.totalScore = totalScore;
            return this;
        }

        public AssessmentAnswersBuilder actualScoreInLevel(int actualScoreInLevel) {
            this.actualScoreInLevel = actualScoreInLevel;
            return this;
        }

        public AssessmentAnswersBuilder level(long level) {
            this.level = level;
            return this;
        }

        public AssessmentAnswersBuilder answers(String answers) {
            this.answers = answers;
            return this;
        }

        public AssessmentAnswersBuilder userRefId(long userRefId) {
            this.userRefId = userRefId;
            return this;
        }

        public AssessmentAnswersBuilder iss(String iss) {
            this.iss = iss;
            return this;
        }

        public AssessmentAnswers build() {
            return new AssessmentAnswers(this);
        }
    }

    @Override
    public String toString() {
        return "AssessmentAnswers{" +
                "sandboxId=" + sandboxId +
                ", trainingDefinitionId=" + trainingDefinitionId +
                ", trainingInstanceId=" + trainingInstanceId +
                ", trainingRunId=" + trainingRunId +
                ", gameTime=" + gameTime +
                ", totalScore=" + totalScore +
                ", actualScoreInLevel=" + actualScoreInLevel +
                ", level=" + level +
                ", answers='" + answers + '\'' +
                ", userRefId=" + userRefId +
                ", iss='" + iss + '\'' +
                '}';
    }
}
