package cz.muni.csirt.kypo.events.trainings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import cz.muni.csirt.kypo.elasticsearch.AbstractAuditPOJO;
import cz.muni.csirt.kypo.events.trainings.enums.LevelType;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

/**
 * This classes uses Builder pattern based on the following blog:
 *
 * @author Pavel Šeda
 * @see <a href="https://blog.jayway.com/2012/02/07/builder-pattern-with-a-twist/">https://blog.jayway.com/2012/02/07/builder-pattern-with-a-twist/</a>
 * <p>
 * Without that builder it is easy to mesh class parameters, e.g. trainingDefinitionId with trainingInstanceId.
 */
@ApiObject(name = "Level Completed", description = "Type of event from trainings.")
@JsonRootName(value = "event")
public class LevelCompleted extends AbstractAuditPOJO {

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
    @ApiObjectField(description = "The result score of player earned in particular level.")
    @JsonProperty(value = "score", required = true)
    private int score;

    public static SandboxIdBuilder builder() {
        return new LevelCompletedBuilder();
    }

    public static class LevelCompletedBuilder implements SandboxIdBuilder, TrainingDefinitionIdBuilder, TrainingInstanceIdBuilder, TrainingRunIdBuilder, PlayerLoginBuilder, LevelBuilder, LevelTypeBuilder, ScoreBuilder {
        private long sandboxId;
        private long trainingDefinitionId;
        private long trainingInstanceId;
        private long trainingRunId;
        private String playerLogin;
        private long level;
        private LevelType levelType;
        private int score;

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
        public LevelBuilder playerLogin(String playerLogin) {
            this.playerLogin = playerLogin;
            return this;
        }

        @Override
        public LevelTypeBuilder level(long level) {
            this.level = level;
            return this;
        }

        @Override
        public ScoreBuilder levelType(LevelType levelType) {
            this.levelType = levelType;
            return this;
        }

        @Override
        public LevelCompletedBuilder score(int score) {
            this.score = score;
            return this;
        }

        public LevelCompleted build() {
            return new LevelCompleted(this);
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
        LevelBuilder playerLogin(String playerLogin);
    }

    public interface LevelBuilder {
        LevelTypeBuilder level(long level);
    }

    public interface LevelTypeBuilder {
        ScoreBuilder levelType(LevelType levelType);
    }

    public interface ScoreBuilder {
        LevelCompletedBuilder score(int score);
    }

    private LevelCompleted(LevelCompletedBuilder builder) {
        this.sandboxId = builder.sandboxId;
        this.trainingDefinitionId = builder.trainingDefinitionId;
        this.trainingInstanceId = builder.trainingInstanceId;
        this.trainingRunId = builder.trainingRunId;
        this.playerLogin = builder.playerLogin;
        this.level = builder.level;
        this.levelType = builder.levelType;
        this.score = builder.score;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "LevelCompleted{" +
                "sandboxId=" + sandboxId +
                ", trainingDefinitionId=" + trainingDefinitionId +
                ", trainingInstanceId=" + trainingInstanceId +
                ", trainingRunId=" + trainingRunId +
                ", playerLogin='" + playerLogin + '\'' +
                ", level=" + level +
                ", levelType=" + levelType +
                ", score=" + score +
                '}';
    }
}
